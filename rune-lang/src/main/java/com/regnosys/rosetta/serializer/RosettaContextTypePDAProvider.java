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
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.serializer.analysis.ContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.ISerState;
import org.eclipse.xtext.serializer.analysis.SerializationContextMap;
import org.eclipse.xtext.util.formallang.Pda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

/**
 * Serialises access to Xtext's context-type PDA cache. The upstream provider is
 * a singleton with a plain {@link java.util.HashMap}; concurrent reads can miss
 * the cache together and mutate it concurrently.
 *
 * TODO: contribute this synchronization, or an equivalent
 * {@code ConcurrentHashMap.computeIfAbsent} implementation to Xtext.
 */
@Singleton
public class RosettaContextTypePDAProvider extends ContextTypePDAProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(RosettaContextTypePDAProvider.class);

	private CompletableFuture<Void> warmUp;

	public synchronized CompletableFuture<Void> warmUpAsync(Grammar grammar) {
		if (warmUp == null) {
			warmUp = CompletableFuture.runAsync(() -> runWarmUp(grammar));
		}
		return warmUp;
	}

	private void runWarmUp(Grammar grammar) {
		try {
			long started = System.nanoTime();
			getContextTypePDAs(grammar);
			long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
			LOGGER.debug("Warmed up serializer context-type PDAs in {} ms.", elapsedMs);
		} catch (Exception e) {
			LOGGER.warn("Failed to warm up serializer context-type PDAs.", e);
		}
	}

	@Override
	public synchronized SerializationContextMap<Pda<ISerState, RuleCall>> getContextTypePDAs(Grammar grammar) {
		return super.getContextTypePDAs(grammar);
	}
}
