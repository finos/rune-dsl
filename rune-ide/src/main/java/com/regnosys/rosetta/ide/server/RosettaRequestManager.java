/*
 * Copyright 2024 REGnosys
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

package com.regnosys.rosetta.ide.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.regnosys.rosetta.cache.RequestScopedCacheManager;

import org.eclipse.xtext.ide.server.concurrent.AbstractRequest;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/**
 * A request manager that will time out after a configurable amount of seconds.
 * It can be configured through an environment variable.
 */
@Singleton
public class RosettaRequestManager extends RequestManager {
	public static String TIMEOUT_ENV_NAME = "ROSETTA_LANGUAGE_SERVER_REQUEST_TIMEOUT";
		
	private final Duration timeout;
	private final ScheduledExecutorService scheduler =
	        Executors.newScheduledThreadPool(
	                1,
	                new ThreadFactoryBuilder()
	                        .setDaemon(true)
	                        .setNameFormat("rosetta-language-server-request-timeout-%d")
	                        .build());
	private final RequestScopedCacheManager requestCacheManager;

	/*
	 * TODO: contribute to Xtext
	 * The code that uses this list fixes a memory leak in the RequestManager and should be contributed
	 * back to the Xtext project then removed from here
	 */
	/* @ProtectedForTesting */
	protected List<AbstractRequest<?>> removableRequestList = new CopyOnWriteArrayList<>();

	@Inject
	public RosettaRequestManager(ExecutorService parallel, OperationCanceledManager operationCanceledManager, IResourceServiceProvider.Registry serviceProviderRegistry) {
		super(parallel, operationCanceledManager);
		
		String rawTimeout = System.getenv(TIMEOUT_ENV_NAME);
		if (rawTimeout != null) {
			this.timeout = Duration.ofSeconds(Long.parseLong(rawTimeout));
		} else {
			this.timeout = null;
		}
		
		Object serviceProvider = serviceProviderRegistry.getExtensionToFactoryMap().get("rosetta");
		if (serviceProvider instanceof IResourceServiceProvider) {
			this.requestCacheManager = ((IResourceServiceProvider) serviceProvider).get(RequestScopedCacheManager.class);
		} else {
			this.requestCacheManager = null;
		}
	}

	@Override
	protected <V> CompletableFuture<V> submit(AbstractRequest<V> request) {
		addRequest(request);
		submitRequest(request);
		return request.get().whenComplete((result, error) -> removableRequestList.remove(request));
	}

	@Override
	protected void addRequest(AbstractRequest<?> request) {
		removableRequestList.add(request);
	}

	@Override
	protected CompletableFuture<Void> cancel() {
        List<AbstractRequest<?>> oldRequests = removableRequestList;
        removableRequestList = new CopyOnWriteArrayList<>();
        // Create a snapshot to avoid concurrent modification during iteration, e.g., elements being removed during iteration.
        List<AbstractRequest<?>> localRequests = List.copyOf(oldRequests);

        CompletableFuture<?>[] cfs = new CompletableFuture<?>[localRequests.size()];
		for (int i = 0, max = localRequests.size(); i < max; i++) {
			AbstractRequest<?> request = localRequests.get(i);
			request.cancel();
			cfs[i] = request.get();
		}
        return CompletableFuture.allOf(cfs);
	}
	
	@Override
	public <V> CompletableFuture<V> runRead(Function1<? super CancelIndicator, ? extends V> cancellable) {
		 return withTimeout(super.runRead(cancellable));
	}

	@Override
	public <U, V> CompletableFuture<V> runWrite(
			Function0<? extends U> nonCancellable,
			Function2<? super CancelIndicator, ? super U, ? extends V> cancellable) {
		return super.runWrite(() -> {
			if (requestCacheManager != null) {
				requestCacheManager.clearAll();
			}
			return nonCancellable.apply();
		}, (cancelIndicator, intermediate) -> runCancellableWithTimeout((_cancelIndicator) -> cancellable.apply(_cancelIndicator, intermediate)).apply(cancelIndicator));
	}
	
    private <V> CompletableFuture<V> withTimeout(CompletableFuture<V> future) {
        if (timeout == null) {
            return future;
        }
        return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
	private <V> Function1<? super CancelIndicator, ? extends V> runCancellableWithTimeout(Function1<? super CancelIndicator, ? extends V> cancellable) {
		return (cancelIndicator) -> {
			try {
		    	if (timeout == null) {
		    		return cancellable.apply(cancelIndicator);
		    	}
				return CompletableFuture.supplyAsync(
						() -> cancellable.apply(cancelIndicator),
						scheduler
					).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).join();
            } catch (CompletionException ex) {
                // Unwrap to retain original cause semantics for callers
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (cause instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(cause);
            }
        };
	}
}
