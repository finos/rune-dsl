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

import java.util.concurrent.CompletableFuture;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.analysis.IContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IConstraint;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IFeatureInfo;
import org.eclipse.xtext.serializer.analysis.ISemanticSequencerNfaProvider;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider;
import org.eclipse.xtext.serializer.analysis.SerializationContextMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Builds the serializer's static analysis of the grammar once, on one thread, before anything is
 * allowed to serialize.
 *
 * <p>Xtext builds that analysis lazily during the first serialization and caches it in state that is
 * not thread safe. Two kinds of state, with different shapes:
 *
 * <p>Grammar-level state is the four per-grammar caches in the analysis providers, the shared
 * {@code GrammarElementDeclarationOrder} adapter on the grammar, and the context index inside each
 * {@link SerializationContextMap}. An injector has exactly one grammar, so each of these holds one
 * entry ever and any call that reaches it once closes it permanently. {@link #build()} closes all of
 * them by asking for the grammar constraints, which pull the other three providers in dependency
 * order.
 *
 * <p>Per-constraint state is {@code Constraint.getFeatures()}, {@code Constraint.getBody()} and
 * {@code FeatureInfo.getAssignments()}, computed per constraint on the serialize path, so a model
 * closes only the constraints it happens to reach. Each of the three assigns its container to the
 * field and only then fills it, so a second thread can be handed a container that is present but
 * empty — and {@code ContextFinder} treats an empty feature slot as "this constraint does not
 * apply", silently dropping a valid constraint and emitting different text rather than throwing.
 * {@link #forceConstraintState} closes those too, which is the one part the sequencing alone cannot
 * do.
 *
 * <p>Callers do not have to know any of this: {@link RosettaSerializer} calls {@link #ensureWarm()}
 * on every serialization, and after the first one that is a single volatile read. Warming up in
 * advance is a latency measure only — it moves several seconds off whichever request would otherwise
 * have paid them.
 */
@Singleton
public class SerializerAnalysisWarmUp {

	// TODO: fix this in Xtext and delete both this class and the gate in RosettaSerializer. The four
	// per-grammar caches want ConcurrentHashMap.computeIfAbsent, GrammarElementDeclarationOrder.get
	// wants synchronizing, and SerializationContextMap.get, Constraint.getFeatures,
	// Constraint.getBody and FeatureInfo.getAssignments each want their value built into a local and
	// assigned to the field afterwards rather than before. Upgrading will not do it for us: the same
	// unsynchronized publication is still there in 2.41.0, three minor versions on, and no upstream
	// issue is open for it.

	private static final Logger LOGGER = LoggerFactory.getLogger(SerializerAnalysisWarmUp.class);

	private final IGrammarAccess grammarAccess;
	private final IGrammarConstraintProvider constraintProvider;
	private final ISemanticSequencerNfaProvider nfaProvider;
	private final ISyntacticSequencerPDAProvider syntacticPdaProvider;
	private final IContextTypePDAProvider contextTypePdaProvider;

	/**
	 * Set before {@link #build()} runs, and read only under the monitor. This is what makes the gate
	 * re-entrant: anything {@code build()} itself does that serializes would otherwise deadlock on a
	 * monitor its own thread already holds.
	 */
	private boolean warm;

	/**
	 * Set after {@link #build()} returns, and read without the monitor. A thread that sees this set
	 * knows the analysis is finished rather than merely started, so it can skip the monitor entirely.
	 * Being volatile is what makes the writes {@code build()} performed visible to it.
	 */
	private volatile boolean published;

	private CompletableFuture<Void> eagerWarmUp;

	@Inject
	public SerializerAnalysisWarmUp(IGrammarAccess grammarAccess, IGrammarConstraintProvider constraintProvider,
			ISemanticSequencerNfaProvider nfaProvider, ISyntacticSequencerPDAProvider syntacticPdaProvider,
			IContextTypePDAProvider contextTypePdaProvider) {
		this.grammarAccess = grammarAccess;
		this.constraintProvider = constraintProvider;
		this.nfaProvider = nfaProvider;
		this.syntacticPdaProvider = syntacticPdaProvider;
		this.contextTypePdaProvider = contextTypePdaProvider;
	}

	/**
	 * Builds the analysis if it is not built, and returns once it is. Called before every
	 * serialization; after the first it costs one volatile read.
	 */
	public void ensureWarm() {
		if (published) {
			return;
		}
		synchronized (this) {
			if (warm) {
				return;
			}
			// Set before build(), not after. A monitor is re-entrant, so anything build() does that
			// serializes comes back through here on the same thread and passes straight into the
			// synchronized block; without the flag already set it would call build() again, and again.
			warm = true;
			try {
				build();
			} finally {
				published = true;
			}
		}
	}

	/**
	 * Builds the analysis on the calling thread, returning once it is ready to be used from any
	 * thread. A standalone consumer about to serialize from several threads can call this to pay the
	 * cost up front, but does not have to: {@link #ensureWarm()} makes every serialization safe on its
	 * own, so skipping this is slower on the first serialization and never less safe.
	 * <p>
	 * That consumer can hold one serializer and call it from all of those threads. Xtext's sequencers
	 * keep mutable state for the length of one serialization, but {@code Serializer} takes them as
	 * {@code Provider} fields and asks for a fresh set inside each call, and this grammar binds them
	 * unscoped, so no sequencer is ever shared between two calls.
	 */
	public void warmUp() {
		long started = System.nanoTime();
		ensureWarm();
		long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
		LOGGER.debug("Warmed up the serializer analysis in {} ms.", elapsedMs);
	}

	/**
	 * Starts the warm-up on a background thread, at most once per injector, and returns the future of
	 * that single run. Failures are logged rather than propagated: a failed warm-up costs time, not
	 * correctness, because the gate builds the analysis on demand anyway.
	 */
	public synchronized CompletableFuture<Void> warmUpAsync() {
		if (eagerWarmUp == null) {
			eagerWarmUp = CompletableFuture.runAsync(this::runWarmUp);
		}
		return eagerWarmUp;
	}

	private void runWarmUp() {
		try {
			warmUp();
		} catch (Exception e) {
			LOGGER.warn("Failed to warm up the serializer analysis.", e);
		}
	}

	/**
	 * Asking for the grammar constraints fills all four provider caches: the constraints need the
	 * semantic sequencer NFAs, which need the syntactic sequencer PDAs, which need the context type
	 * PDAs. It also installs the {@code GrammarElementDeclarationOrder} adapter on the grammar, so the
	 * later reads of it from the serialize path are cache hits that mutate nothing.
	 *
	 * <p>Each of the four analyses is then asked for one context. Filling the provider caches is not
	 * the same as finishing the analyses: a {@link SerializationContextMap} indexes its contents on
	 * the first {@code get()}, and for three of the four that first {@code get()} would otherwise
	 * happen during a serialization rather than here — the syntactic sequencer looks its context up
	 * per object, the semantic one per object too. Asking here is what puts them on this thread. One
	 * ask per map is enough, because the index is built for every context in a single pass.
	 */
	// Overridable so a test can pin the re-entrancy ordering below; not an extension point.
	protected void build() {
		Grammar grammar = grammarAccess.getGrammar();
		// getConstraints below already forces this one, by naming each constraint through
		// findBestConstraintName, which reads the index at GrammarConstraintProvider:564. Asked for
		// anyway, so that the gate does not depend on Xtext naming constraints that way.
		indexByContext(contextTypePdaProvider.getContextTypePDAs(grammar));
		indexByContext(syntacticPdaProvider.getSyntacticSequencerPDAs(grammar));
		indexByContext(nfaProvider.getSemanticSequencerNFAs(grammar));
		SerializationContextMap<IConstraint> constraints = constraintProvider.getConstraints(grammar);
		indexByContext(constraints);
		forceConstraintState(constraints);
	}

	/**
	 * Forces every constraint's body, features and assignments, and the context index of the map
	 * holding them.
	 *
	 * <p>{@link IConstraint#getFeatures} is documented as leaving a null slot for a feature with no
	 * assignment. In 2.38.0 the one implementation fills every slot, so this loop should not meet a
	 * null — but the contract allows one, and {@code ContextFinder} reads the array expecting one, so
	 * this skips rather than throws. Forcing is an optimisation of when work happens; it should not be
	 * the thing that turns a tolerated null into a crash.
	 */
	private static void forceConstraintState(SerializationContextMap<IConstraint> constraints) {
		for (SerializationContextMap.Entry<IConstraint> entry : constraints.values()) {
			IConstraint constraint = entry.getValue();
			// getAssignments() reaches getBody() as well, but only for a constraint that has at least
			// one feature. Asking directly is what makes it closed for all of them.
			constraint.getBody();
			for (IFeatureInfo feature : constraint.getFeatures()) {
				if (feature != null) {
					feature.getAssignments();
				}
			}
		}
	}

	/**
	 * Asks one analysis for one context, which builds its index for every context. An analysis with no
	 * entries is left alone: there is no context to ask for, and an index that never receives an entry
	 * cannot be read half-built.
	 */
	private static void indexByContext(SerializationContextMap<?> analysis) {
		for (SerializationContextMap.Entry<?> entry : analysis.values()) {
			for (ISerializationContext context : entry.getContexts()) {
				analysis.get(context);
				return;
			}
		}
	}
}
