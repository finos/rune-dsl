package com.regnosys.rosetta.ide.server;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
		
	@Inject
	public RosettaRequestManager(ExecutorService parallel, OperationCanceledManager operationCanceledManager) {
		super(parallel, operationCanceledManager);
		
		String rawTimeout = System.getenv(TIMEOUT_ENV_NAME);
		if (rawTimeout != null) {
			this.timeout = Duration.ofSeconds(Long.parseLong(rawTimeout));
		} else {
			this.timeout = null;
		}
	}
	
	@Override
	public <V> CompletableFuture<V> runRead(Function1<? super CancelIndicator, ? extends V> cancellable) {
		return super.runRead((cancelIndicator) -> {		    
		    try {
		    	if (timeout == null) {
		    		return cancellable.apply(cancelIndicator);
		    	}
				return CompletableFuture.supplyAsync(
						() -> cancellable.apply(cancelIndicator),
						scheduler
					).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException)ex;
				}
				throw new RuntimeException(ex);
			}
		});
	}

	@Override
	public <U, V> CompletableFuture<V> runWrite(
			Function0<? extends U> nonCancellable,
			Function2<? super CancelIndicator, ? super U, ? extends V> cancellable) {
		return super.runWrite(nonCancellable, (cancelIndicator, intermediate) -> {		    
		    try {
		    	if (timeout == null) {
		    		return cancellable.apply(cancelIndicator, intermediate);
		    	}
				return CompletableFuture.supplyAsync(
						() -> cancellable.apply(cancelIndicator, intermediate),
						scheduler
					).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException)ex;
				}
				throw new RuntimeException(ex);
			}
		});
	}
}
