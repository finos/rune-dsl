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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.serializer.analysis.ContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.IContextTypePDAProvider;
import org.eclipse.xtext.serializer.analysis.ISerState;
import org.eclipse.xtext.serializer.analysis.SerializationContextMap;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.util.formallang.Pda;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Injector;
import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;

import jakarta.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
class RosettaContextTypePDAProviderTest {
	@Inject
	private IContextTypePDAProvider contextTypePDAProvider;
	@Inject
	private ContextTypePDAProvider concreteContextTypePDAProvider;
	@Inject
	private IGrammarAccess grammarAccess;
	@Inject
	private Injector injector;

	@Test
	void bindsRosettaContextTypePDAProvider() {
		assertInstanceOf(RosettaContextTypePDAProvider.class, contextTypePDAProvider);
		assertSame(contextTypePDAProvider, concreteContextTypePDAProvider);
	}

	@Test
	void warmUpPopulatesTheProviderCache() throws Exception {
		RosettaContextTypePDAProvider provider =
				assertInstanceOf(RosettaContextTypePDAProvider.class, contextTypePDAProvider);
		Grammar grammar = grammarAccess.getGrammar();
		CompletableFuture<Void> first = provider.warmUpAsync(grammar);
		CompletableFuture<Void> second = provider.warmUpAsync(grammar);

		assertSame(first, second);
		first.get(60, TimeUnit.SECONDS);
		assertTrue(cache(provider).containsKey(grammar));
	}

	@Test
	void concurrentCallsWaitForFirstComputationAndReuseCache() throws Exception {
		BlockingProvider provider = new BlockingProvider();
		injector.injectMembers(provider);
		Grammar grammar = grammarAccess.getGrammar();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<SerializationContextMap<Pda<ISerState, RuleCall>>> first =
					executor.submit(() -> provider.getContextTypePDAs(grammar));
			assertTrue(provider.enteredComputation.await(30, TimeUnit.SECONDS));

			Future<SerializationContextMap<Pda<ISerState, RuleCall>>> second =
					executor.submit(() -> provider.getContextTypePDAs(grammar));
			Thread.sleep(100);
			assertFalse(second.isDone());

			provider.releaseComputation.countDown();
			SerializationContextMap<Pda<ISerState, RuleCall>> firstResult = first.get(60, TimeUnit.SECONDS);
			int collectTypesCallsAfterFirstResult = provider.collectTypesCalls.get();
			SerializationContextMap<Pda<ISerState, RuleCall>> secondResult = second.get(60, TimeUnit.SECONDS);

			assertSame(firstResult, secondResult);
			assertEquals(collectTypesCallsAfterFirstResult, provider.collectTypesCalls.get());
			assertEquals(1, provider.maxConcurrentCollectTypes.get());
		} finally {
			provider.releaseComputation.countDown();
			executor.shutdownNow();
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Grammar, ?> cache(RosettaContextTypePDAProvider provider) throws Exception {
		Field cache = ContextTypePDAProvider.class.getDeclaredField("cache");
		cache.setAccessible(true);
		return (Map<Grammar, ?>) cache.get(provider);
	}

	private static class BlockingProvider extends RosettaContextTypePDAProvider {
		private final AtomicBoolean blockOnce = new AtomicBoolean(true);
		private final AtomicInteger activeCollectTypes = new AtomicInteger();
		private final AtomicInteger maxConcurrentCollectTypes = new AtomicInteger();
		private final AtomicInteger collectTypesCalls = new AtomicInteger();
		private final CountDownLatch enteredComputation = new CountDownLatch(1);
		private final CountDownLatch releaseComputation = new CountDownLatch(1);

		@Override
		synchronized CompletableFuture<Void> warmUpAsync(Grammar grammar) {
			return CompletableFuture.completedFuture(null);
		}

		@Override
		protected Set<EClass> collectTypes(Pda<ISerState, RuleCall> contextPda, Map<ISerState, Integer> distances) {
			int active = activeCollectTypes.incrementAndGet();
			maxConcurrentCollectTypes.updateAndGet(current -> Math.max(current, active));
			collectTypesCalls.incrementAndGet();
			try {
				if (blockOnce.compareAndSet(true, false)) {
					enteredComputation.countDown();
					assertTrue(releaseComputation.await(30, TimeUnit.SECONDS));
				}
				return super.collectTypes(contextPda, distances);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			} finally {
				activeCollectTypes.decrementAndGet();
			}
		}
	}
}
