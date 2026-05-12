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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
 * the cache together and mutate it concurrently. Each provider keeps its own
 * cache, but the speculative background warm-up is throttled across the JVM so
 * short-lived injectors do not start many expensive PDA builds in parallel.
 *
 * TODO: contribute this synchronization, or an equivalent
 * {@code ConcurrentHashMap.computeIfAbsent} implementation, and the
 * grammar PDA warm-up hook/throttle, to Xtext.
 */
@Singleton
public class RosettaContextTypePDAProvider extends ContextTypePDAProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(RosettaContextTypePDAProvider.class);
	// These are static because a single JVM can host many injectors (usually during testing). To prevent
	// memory consumption for each running test, warmup is skipped if multiple threads try to run it at the same time.
	private static final AtomicBoolean WARM_UP_RUNNING = new AtomicBoolean();
	private static final AtomicInteger WARM_UP_THREAD_COUNTER = new AtomicInteger();
	private static final CompletableFuture<Void> SKIPPED_WARM_UP = CompletableFuture.completedFuture(null);
	// A dedicated single daemon thread prevents warm-up from flooding the common pool.
	private static final ExecutorService WARM_UP_EXECUTOR = new ThreadPoolExecutor(
			1,
			1,
			0L,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(1),
			task -> {
				Thread thread = new Thread(
						task,
						"rosetta-serializer-warmup-" + WARM_UP_THREAD_COUNTER.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			});

	private CompletableFuture<Void> warmUp;

	public synchronized CompletableFuture<Void> warmUpAsync(Grammar grammar) {
		if (warmUp == null) {
			CompletableFuture<Void> startedWarmUp = startWarmUp(grammar);
			if (startedWarmUp == null) {
				return SKIPPED_WARM_UP;
			}
			warmUp = startedWarmUp;
		}
		return warmUp;
	}

	private CompletableFuture<Void> startWarmUp(Grammar grammar) {
		if (!WARM_UP_RUNNING.compareAndSet(false, true)) {
			LOGGER.debug("Skipping serializer context-type PDA warm-up because another warm-up is already running.");
			return null;
		}
		CompletableFuture<Void> result = new CompletableFuture<>();
		try {
			WARM_UP_EXECUTOR.execute(() -> runWarmUp(grammar, result));
		} catch (RuntimeException e) {
			WARM_UP_RUNNING.set(false);
			result.completeExceptionally(e);
			throw e;
		}
		return result;
	}

	private void runWarmUp(Grammar grammar, CompletableFuture<Void> result) {
		try {
			long started = System.nanoTime();
			getContextTypePDAs(grammar);
			long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
			LOGGER.debug("Warmed up serializer context-type PDAs in {} ms.", elapsedMs);
			result.complete(null);
		} catch (Exception e) {
			LOGGER.warn("Failed to warm up serializer context-type PDAs.", e);
			result.complete(null);
		} catch (Error e) {
			result.completeExceptionally(e);
			throw e;
		} finally {
			WARM_UP_RUNNING.set(false);
		}
	}

	@Override
	public synchronized SerializationContextMap<Pda<ISerState, RuleCall>> getContextTypePDAs(Grammar grammar) {
		return super.getContextTypePDAs(grammar);
	}
}
