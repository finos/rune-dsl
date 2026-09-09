/*
 * Copyright 2026 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.serializer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.serializer.analysis.ContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.GrammarConstraintProvider;
import org.eclipse.xtext.serializer.analysis.IContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IConstraint;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IFeatureInfo;
import org.eclipse.xtext.serializer.analysis.ISemanticSequencerNfaProvider;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider;
import org.eclipse.xtext.serializer.analysis.SemanticSequencerNfaProvider;
import org.eclipse.xtext.serializer.analysis.SerializationContextMap;
import org.eclipse.xtext.serializer.analysis.SyntacticSequencerPDAProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.inject.Injector;
import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;

/**
 * Covers the one thing that makes serializing safe from several threads: that the grammar analysis
 * is built once, on one thread, before any serialization uses it.
 *
 * <p>Every test here needs an injector whose analysis has not been built yet, because that is the
 * only state in which the defect is reachable. The injector Xtext's {@code InjectionExtension}
 * hands out is shared across a whole test run and warm long before this class runs, so each test
 * builds its own and asserts it really is cold.
 *
 * <p>The class lives in {@code rune-ide} because the injector provider does; everything it exercises
 * is in {@code rune-lang}.
 */
class SerializerAnalysisTest {

	private static final int THREADS = 16;

	private static final String MODEL = """
			namespace test.serializer

			type Foo:
			    bar string (1..1)
			""";

	private RosettaIdeInjectorProvider injectorProvider;
	private Injector injector;

	@BeforeEach
	void createColdInjector() throws Exception {
		injectorProvider = new RosettaIdeInjectorProvider();
		injectorProvider.setupRegistry();
		injector = injectorProvider.getInjector();
		assertColdInjector();
	}

	@AfterEach
	void restoreRegistry() {
		injectorProvider.restoreRegistry();
	}

	@Test
	void bindsTheRosettaSerializerUnderBothTypes() {
		// The gate lives in the serializer, so a language that binds Xtext's own has no gate at all.
		//
		// Both types, because Guice will happily build an unbound concrete class on demand and hand
		// back a plain Serializer with no gate in it. That is not a hypothetical injection site:
		// serializeToRegions and serializeReplacement are absent from ISerializer, so a caller wanting
		// either has to ask for the concrete class, and XtextResourceFormatter does.
		assertAll(
				() -> assertInstanceOf(RosettaSerializer.class, injector.getInstance(ISerializer.class)),
				() -> assertInstanceOf(RosettaSerializer.class, injector.getInstance(Serializer.class)));
	}

	@Test
	void everyPublicSerializePathReachesTheGate() throws Exception {
		// This is the whole design's single point of failure. Xtext's Serializer funnels every public
		// entry point through getIContext before it touches a sequencer, which is why one override
		// covers all of them — but nothing in Xtext promises to keep doing that, and a path that
		// stopped passing through would serialize on an unbuilt analysis with nothing else to notice.
		// Typed concretely on purpose: serializeToRegions and serializeReplacement are not on
		// ISerializer, which is why the gate goes in getIContext rather than around that interface.
		RosettaSerializer serializer = (RosettaSerializer) injector.getInstance(ISerializer.class);
		CountingWarmUp gate = interceptGate(serializer);
		EObject model = parse();

		assertAll(
				() -> assertGateReached("serialize(EObject)", gate, () -> serializer.serialize(model)),
				() -> assertGateReached("serialize(EObject, SaveOptions)", gate,
						() -> serializer.serialize(model, SaveOptions.defaultOptions())),
				() -> assertGateReached("serialize(EObject, Writer, SaveOptions)", gate,
						() -> serializer.serialize(model, new StringWriter(), SaveOptions.defaultOptions())),
				() -> assertGateReached("serializeToRegions(EObject)", gate,
						() -> serializer.serializeToRegions(model)),
				() -> assertGateReached("serializeReplacement(EObject, SaveOptions)", gate,
						() -> serializer.serializeReplacement(model, SaveOptions.defaultOptions())));
	}

	@Test
	void theGateBuildsTheWholeAnalysisBeforeAnythingSerializes() throws Exception {
		Grammar grammar = injector.getInstance(IGrammarAccess.class).getGrammar();

		// Deliberately not through a serialization. Serializing fills some of this on the way past, so
		// a test that serialized first could not tell state the gate built from state the serialization
		// built — and state the serialization builds is exactly the state two of them race over.
		injector.getInstance(SerializerAnalysisWarmUp.class).warmUp();

		// Filling the four provider caches is not the same as finishing the analysis. Each analysis
		// indexes its contexts on first read, and each constraint computes its own body, features and
		// assignments on first read; all of that has to happen inside the gate, or it happens on
		// whichever thread gets there first.
		assertAll(
				() -> assertTrue(cache(IContextTypePDAProvider.class, ContextTypePDAProvider.class)
						.containsKey(grammar)),
				() -> assertTrue(cache(ISyntacticSequencerPDAProvider.class, SyntacticSequencerPDAProvider.class)
						.containsKey(grammar)),
				() -> assertTrue(cache(ISemanticSequencerNfaProvider.class, SemanticSequencerNfaProvider.class)
						.containsKey(grammar)),
				() -> assertTrue(cache(IGrammarConstraintProvider.class, GrammarConstraintProvider.class)
						.containsKey(grammar)),
				() -> assertContextIndexBuilt("context type PDAs",
						cached(IContextTypePDAProvider.class, ContextTypePDAProvider.class, grammar)),
				() -> assertContextIndexBuilt("syntactic sequencer PDAs",
						cached(ISyntacticSequencerPDAProvider.class, SyntacticSequencerPDAProvider.class, grammar)),
				() -> assertContextIndexBuilt("semantic sequencer NFAs",
						cached(ISemanticSequencerNfaProvider.class, SemanticSequencerNfaProvider.class, grammar)),
				() -> assertContextIndexBuilt("grammar constraints",
						cached(IGrammarConstraintProvider.class, GrammarConstraintProvider.class, grammar)),
				this::assertConstraintStateForced);
	}

	@Test
	void concurrentFirstSerializationGivesEveryThreadTheSameText() throws Exception {
		// The net for the whole design. Sixteen threads serialize the same model on a cold injector,
		// released together, so they all arrive at the unbuilt analysis at once. Without the gate one
		// of them reads a cache another is still filling, and the run either throws or emits text that
		// differs from the rest.
		EObject model = parse();
		List<ISerializer> serializers = new ArrayList<>();
		for (int i = 0; i < THREADS; i++) {
			// One serializer per thread, so the only thing they share is the injector's analysis — the
			// state under test. Sharing one instance is safe too, and has its own test below.
			serializers.add(injector.getInstance(ISerializer.class));
		}

		CyclicBarrier start = new CyclicBarrier(THREADS);
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		try {
			List<Future<String>> results = new ArrayList<>();
			for (ISerializer serializer : serializers) {
				results.add(executor.submit(() -> {
					start.await(2, TimeUnit.MINUTES);
					return serializer.serialize(model);
				}));
			}

			String expected = results.get(0).get(10, TimeUnit.MINUTES);
			for (Future<String> result : results) {
				assertEquals(expected, result.get(10, TimeUnit.MINUTES),
						"Threads serializing together on a cold injector produced different text.");
			}
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void oneSharedSerializerIsSafeFromSeveralThreads() throws Exception {
		// Sixteen threads share a single serializer on a cold injector. The mutable state on
		// AbstractSyntacticSequencer:98-100 belongs to one serialize call, not to the instance: Serializer
		// holds its three sequencers as Provider fields and asks for a fresh set inside each call
		// (Serializer:114-116), and this grammar binds them unscoped. The one thing genuinely shared is
		// the ContextFinder, whose lazy constraints field (ContextFinder:266-267) is assigned the map the
		// gate has already built. Removing the gate fails this with a null antlrNameToRule.
		EObject model = parse();
		ISerializer shared = injector.getInstance(ISerializer.class);

		CyclicBarrier start = new CyclicBarrier(THREADS);
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		try {
			List<Future<String>> results = new ArrayList<>();
			for (int i = 0; i < THREADS; i++) {
				results.add(executor.submit(() -> {
					start.await(2, TimeUnit.MINUTES);
					return shared.serialize(model);
				}));
			}

			String expected = results.get(0).get(10, TimeUnit.MINUTES);
			for (Future<String> result : results) {
				assertEquals(expected, result.get(10, TimeUnit.MINUTES),
						"Threads sharing one serializer on a cold injector produced different text.");
			}
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void aFailedBuildLeavesTheGateShut() {
		// A build that throws leaves the analysis part-filled, so the gate has to stay shut: publishing
		// on the way out would send every later thread down the fast path onto exactly the state this
		// class exists to prevent, and warmUpAsync logs rather than rethrows, so nothing would say so.
		FailingWarmUp warmUp = new FailingWarmUp();

		assertThrows(IllegalStateException.class, warmUp::ensureWarm);
		assertEquals(1, warmUp.builds.get());

		assertThrows(IllegalStateException.class, warmUp::ensureWarm,
				"The gate opened after a failed build instead of building again.");
		assertEquals(2, warmUp.builds.get(), "The second call skipped the build.");

		// And it still closes once a build succeeds.
		warmUp.fail = false;
		warmUp.ensureWarm();
		assertEquals(3, warmUp.builds.get());
		warmUp.ensureWarm();
		assertEquals(3, warmUp.builds.get(), "The gate stayed open after a successful build.");
	}

	@Test
	void theGateLetsTheBuildItselfSerialize() throws Exception {
		// The gate marks itself warm before it builds, so anything the build does that serializes
		// re-enters and returns. The monitor is re-entrant and never blocks the build thread; what the
		// flag prevents is build() calling itself. Nothing in build() serializes today, so this pins the
		// ordering rather than an existing caller — reversing the two lines fails here with a
		// StackOverflowError instead of failing in whoever adds one.
		ReenteringWarmUp warmUp = new ReenteringWarmUp();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<?> gate = executor.submit(warmUp::ensureWarm);
			gate.get(30, TimeUnit.SECONDS);
			assertTrue(warmUp.reenteredAndReturned, "The nested call never came back.");
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void eachInjectorGetsItsOwnWarmUpAndGrammar() {
		Injector other = new RosettaIdeInjectorProvider().getInjector();

		// The gate is per injector, as it has to be: it guards one grammar's analysis, and a second
		// injector builds its own grammar object with its own analysis to go with it.
		assertNotSame(injector.getInstance(SerializerAnalysisWarmUp.class),
				other.getInstance(SerializerAnalysisWarmUp.class));
		assertNotSame(injector.getInstance(IGrammarAccess.class).getGrammar(),
				other.getInstance(IGrammarAccess.class).getGrammar());
	}

	@Test
	void warmUpStartsOncePerInjector() throws Exception {
		// Two halves: the injector hands out one warm-up, and that warm-up starts one run. Building the
		// blocking double by hand covers only the second, and leaves @Singleton free to go missing.
		assertSame(injector.getInstance(SerializerAnalysisWarmUp.class),
				injector.getInstance(SerializerAnalysisWarmUp.class));

		BlockingWarmUp warmUp = new BlockingWarmUp();
		CompletableFuture<Void> started = warmUp.warmUpAsync();
		assertTrue(warmUp.entered.await(30, TimeUnit.SECONDS));
		assertSame(started, warmUp.warmUpAsync());
		assertEquals(1, warmUp.runs.get());
		warmUp.release.countDown();
		started.get(5, TimeUnit.SECONDS);
		assertEquals(1, warmUp.runs.get());
	}

	private EObject parse() throws Exception {
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		Resource resource = resourceSet.createResource(URI.createURI("serializer-analysis-test.rosetta"));
		resource.load(new ByteArrayInputStream(MODEL.getBytes(UTF_8)), Map.of());
		assertTrue(resource.getErrors().isEmpty(), "The test model does not parse: " + resource.getErrors());
		return resource.getContents().get(0);
	}

	private static void assertGateReached(String path, CountingWarmUp gate, Executable serialize) throws Throwable {
		int before = gate.calls.get();
		serialize.execute();
		assertTrue(gate.calls.get() > before, path + " did not pass through the warm-up gate.");
	}

	/**
	 * Puts a counting warm-up in front of the serializer's own, so the count is of real gate calls
	 * rather than of anything the test overrode.
	 */
	private CountingWarmUp interceptGate(RosettaSerializer serializer) throws Exception {
		CountingWarmUp counting = new CountingWarmUp(injector.getInstance(SerializerAnalysisWarmUp.class));
		Field gate = RosettaSerializer.class.getDeclaredField("warmUp");
		gate.setAccessible(true);
		gate.set(serializer, counting);
		return counting;
	}

	/**
	 * Every test here depends on the injector being cold, and nothing about the code under test makes
	 * it so. Substituting the warm injector that the neighbouring test classes use leaves the
	 * concurrency test passing even with the gate removed, because a built analysis is safe to read
	 * from any number of threads.
	 */
	private void assertColdInjector() throws Exception {
		assertAll(
				() -> assertTrue(cache(IGrammarConstraintProvider.class, GrammarConstraintProvider.class).isEmpty(),
						"The grammar constraints are already built, so this injector is not cold."),
				() -> assertTrue(
						cache(ISemanticSequencerNfaProvider.class, SemanticSequencerNfaProvider.class).isEmpty(),
						"The semantic sequencer NFAs are already built, so this injector is not cold."),
				() -> assertTrue(
						cache(ISyntacticSequencerPDAProvider.class, SyntacticSequencerPDAProvider.class).isEmpty(),
						"The syntactic sequencer PDAs are already built, so this injector is not cold."),
				() -> assertTrue(cache(IContextTypePDAProvider.class, ContextTypePDAProvider.class).isEmpty(),
						"The context type PDAs are already built, so this injector is not cold."));
	}

	private void assertConstraintStateForced() throws Exception {
		Grammar grammar = injector.getInstance(IGrammarAccess.class).getGrammar();
		@SuppressWarnings("unchecked")
		SerializationContextMap<IConstraint> constraints = (SerializationContextMap<IConstraint>) cached(
				IGrammarConstraintProvider.class, GrammarConstraintProvider.class, grammar);

		// ContextFinder reads getFeatures() for every object it serializes, and treats an empty slot as
		// "this constraint does not apply" — so a thread that catches this half-filled drops a valid
		// constraint and emits different text rather than throwing. Nothing downstream would fail,
		// which is why this reads the fields directly.
		assertTrue(!constraints.values().isEmpty(), "No constraints were built, so this proves nothing.");
		List<String> unfilled = new ArrayList<>();
		for (SerializationContextMap.Entry<IConstraint> entry : constraints.values()) {
			IConstraint constraint = entry.getValue();
			// Read each field before its getter, which would fill it and hide the defect. `body` is
			// unbuilt when it still holds Xtext's UNINITIALIZED sentinel; null is a built value there,
			// meaning a constraint with an empty body.
			if (lazyField(constraint, "body") == uninitialisedBody()) {
				unfilled.add(constraint.getName() + ".body");
			}
			if (lazyField(constraint, "features") == null) {
				unfilled.add(constraint.getName() + ".features");
			}
			for (IFeatureInfo feature : constraint.getFeatures()) {
				if (feature != null && lazyField(feature, "assignments") == null) {
					unfilled.add(constraint.getName() + "." + feature.getFeature().getName() + ".assignments");
				}
			}
		}
		assertTrue(unfilled.isEmpty(), "Serialization began with lazy constraint state still unbuilt: " + unfilled);
	}

	private static void assertContextIndexBuilt(String name, SerializationContextMap<?> analysis) throws Exception {
		assertTrue(!analysis.values().isEmpty(), "The " + name + " analysis is empty, so this proves nothing.");
		assertNotNull(lazyField(analysis, "keys"), "The " + name + " were left with an unbuilt context index.");
	}

	/**
	 * The sentinel {@code Constraint.body} holds until it is computed. Read reflectively because it is
	 * private to Xtext, and there is no other way to tell an unbuilt body from a built empty one.
	 */
	private static Object uninitialisedBody() throws Exception {
		Field sentinel = GrammarConstraintProvider.class.getDeclaredField("UNINITIALIZED");
		sentinel.setAccessible(true);
		return sentinel.get(null);
	}

	/**
	 * Reads a lazily-filled Xtext field without going through its getter, which would fill it.
	 */
	private static Object lazyField(Object target, String name) throws Exception {
		for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException keepLooking) {
				// declared further up the hierarchy
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}

	private SerializationContextMap<?> cached(Class<?> boundInterface, Class<?> declaringClass, Grammar grammar)
			throws Exception {
		return (SerializationContextMap<?>) cache(boundInterface, declaringClass).get(grammar);
	}

	@SuppressWarnings("unchecked")
	private Map<Grammar, ?> cache(Class<?> boundInterface, Class<?> declaringClass) throws Exception {
		Field cache = declaringClass.getDeclaredField("cache");
		cache.setAccessible(true);
		return (Map<Grammar, ?>) cache.get(injector.getInstance(boundInterface));
	}

	// The doubles below all pass five nulls to the real constructor. That is safe because each
	// overrides the methods the test uses, and the constructor's arguments are only ever stored in
	// fields those overrides never read.
	private static class CountingWarmUp extends SerializerAnalysisWarmUp {
		private final SerializerAnalysisWarmUp delegate;
		private final AtomicInteger calls = new AtomicInteger();

		CountingWarmUp(SerializerAnalysisWarmUp delegate) {
			super(null, null, null, null, null);
			this.delegate = delegate;
		}

		@Override
		public void ensureWarm() {
			calls.incrementAndGet();
			delegate.ensureWarm();
		}
	}

	/**
	 * A warm-up whose build serializes, which is the case the {@code warm}-before-{@code build()}
	 * ordering exists for.
	 */
	private static class FailingWarmUp extends SerializerAnalysisWarmUp {
		private final AtomicInteger builds = new AtomicInteger();
		private boolean fail = true;

		FailingWarmUp() {
			super(null, null, null, null, null);
		}

		@Override
		protected void build() {
			builds.incrementAndGet();
			if (fail) {
				throw new IllegalStateException("Deliberate build failure.");
			}
		}
	}

	private static class ReenteringWarmUp extends SerializerAnalysisWarmUp {
		private volatile boolean reenteredAndReturned;

		ReenteringWarmUp() {
			super(null, null, null, null, null);
		}

		@Override
		protected void build() {
			ensureWarm();
			reenteredAndReturned = true;
		}
	}

	private static class BlockingWarmUp extends SerializerAnalysisWarmUp {
		private final AtomicInteger runs = new AtomicInteger();
		private final CountDownLatch entered = new CountDownLatch(1);
		private final CountDownLatch release = new CountDownLatch(1);

		BlockingWarmUp() {
			super(null, null, null, null, null);
		}

		@Override
		public void warmUp() {
			runs.incrementAndGet();
			entered.countDown();
			try {
				assertTrue(release.await(5, TimeUnit.MINUTES));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			}
		}
	}
}
