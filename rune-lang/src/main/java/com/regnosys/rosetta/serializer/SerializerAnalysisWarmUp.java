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
 * allowed to serialize. Xtext builds it lazily during the first serialization, into state that is
 * not thread safe. Callers need know none of this: {@link RosettaSerializer} calls
 * {@link #ensureWarm()} on every serialization, and after the first that is one volatile read.
 *
 * <p>Grammar-level state — the four per-grammar provider caches, the
 * {@code GrammarElementDeclarationOrder} adapter, and the context index inside each
 * {@link SerializationContextMap} — holds one entry per injector, so {@link #build()} closes it
 * permanently. Per-constraint state is computed as a model reaches each constraint, so
 * {@link #forceConstraintState} has to walk them: {@code Constraint.getFeatures()} and
 * {@code FeatureInfo.getAssignments()} assign their container and only then fill it, and
 * {@code ContextFinder} reads an empty feature slot as "this constraint does not apply", dropping a
 * valid constraint and emitting different text rather than throwing.
 */
@Singleton
public class SerializerAnalysisWarmUp {

	// TODO: fix this in Xtext and delete both this class and the gate in RosettaSerializer. The four
	// per-grammar caches want ConcurrentHashMap.computeIfAbsent, GrammarElementDeclarationOrder.get
	// wants synchronizing, and SerializationContextMap.get, Constraint.getFeatures and
	// FeatureInfo.getAssignments each want their value built into a local and assigned to the field
	// afterwards rather than before. Constraint.getBody already does that, and wants only a safe
	// publication of the field itself. Upgrading will not do it for us: the same
	// unsynchronized publication is still there in 2.41.0, three minor versions on, and no upstream
	// issue is open for it.

	private static final Logger LOGGER = LoggerFactory.getLogger(SerializerAnalysisWarmUp.class);

	private final IGrammarAccess grammarAccess;
	private final IGrammarConstraintProvider constraintProvider;
	private final ISemanticSequencerNfaProvider nfaProvider;
	private final ISyntacticSequencerPDAProvider syntacticPdaProvider;
	private final IContextTypePDAProvider contextTypePdaProvider;

	/** Set before {@link #build()} runs, read only under the monitor, cleared if it throws. */
	private boolean warm;

	/**
	 * Set only after {@link #build()} returns normally, and read without the monitor: a thread seeing
	 * it set knows the analysis is finished rather than merely started. Volatile so that the writes
	 * {@code build()} made are visible to it.
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
	 * Builds the analysis if it is not built, and returns once it is.
	 */
	public void ensureWarm() {
		if (published) {
			return;
		}
		synchronized (this) {
			if (warm) {
				return;
			}
			// Before build(), not after: the monitor is re-entrant, so anything build() does that
			// serializes walks back in here and would otherwise start a second build.
			warm = true;
			try {
				build();
			} catch (RuntimeException | Error e) {
				// A failed build leaves the analysis part-filled, so the gate stays shut and the next
				// caller retries.
				warm = false;
				throw e;
			}
			published = true;
		}
	}

	/**
	 * Builds the analysis on the calling thread, logging what it cost. Optional — {@link #ensureWarm()}
	 * makes every serialization safe on its own — so this only moves the cost off the first one.
	 */
	public void warmUp() {
		long started = System.nanoTime();
		ensureWarm();
		long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
		LOGGER.debug("Warmed up the serializer analysis in {} ms.", elapsedMs);
	}

	/**
	 * Starts the warm-up on a background thread, at most once per injector, and returns the future of
	 * that single run. Failures are logged rather than propagated, since the gate builds on demand
	 * anyway.
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
	 * Asking for the grammar constraints fills all four provider caches — constraints need the
	 * semantic NFAs, which need the syntactic PDAs, which need the context type PDAs — and installs
	 * the {@code GrammarElementDeclarationOrder} adapter.
	 *
	 * <p>Each analysis is then asked for one context, which builds its whole index in one pass. Two of
	 * the four indexes are read during a serialization in 2.38.0: the syntactic PDAs from
	 * {@code AbstractSyntacticSequencer:337,352,442} and the constraints from {@code ContextFinder:250}.
	 * The semantic NFA map is only ever iterated, and the context type PDAs are already indexed inside
	 * {@code getConstraints} at {@code GrammarConstraintProvider:564}. All four are asked anyway, so a
	 * later Xtext moving one onto the serialize path cannot quietly reopen this.
	 */
	// Overridable so a test can pin the re-entrancy ordering below; not an extension point.
	protected void build() {
		Grammar grammar = grammarAccess.getGrammar();
		indexByContext(contextTypePdaProvider.getContextTypePDAs(grammar));
		indexByContext(syntacticPdaProvider.getSyntacticSequencerPDAs(grammar));
		indexByContext(nfaProvider.getSemanticSequencerNFAs(grammar));
		SerializationContextMap<IConstraint> constraints = constraintProvider.getConstraints(grammar);
		indexByContext(constraints);
		forceConstraintState(constraints);
	}

	/**
	 * Forces every constraint's body, features and assignments. Skips a null feature rather than
	 * throwing, because {@link IConstraint#getFeatures} permits a null slot for a feature with no
	 * assignment even though 2.38.0's implementation fills every one.
	 */
	private static void forceConstraintState(SerializationContextMap<IConstraint> constraints) {
		for (SerializationContextMap.Entry<IConstraint> entry : constraints.values()) {
			IConstraint constraint = entry.getValue();
			// getAssignments() reaches getBody(), but only for a constraint with at least one feature.
			// Asking directly closes it for the rest, and installs GrammarElementDeclarationOrder.
			constraint.getBody();
			for (IFeatureInfo feature : constraint.getFeatures()) {
				if (feature != null) {
					feature.getAssignments();
				}
			}
		}
	}

	/** Asks one analysis for one context, which builds its index for every context. */
	private static void indexByContext(SerializationContextMap<?> analysis) {
		for (SerializationContextMap.Entry<?> entry : analysis.values()) {
			for (ISerializationContext context : entry.getContexts()) {
				analysis.get(context);
				return;
			}
		}
	}
}
