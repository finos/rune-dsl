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

import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.util.CancelIndicator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
class RosettaRequestManagerTest {

	@Inject
	private RosettaRequestManager requestManager;

	@Test
	void testReadRequestsAreClearedFromRequestList()
			throws ExecutionException, InterruptedException, TimeoutException {

		CompletableFuture<Object> completableFuture = requestManager.runRead((cancelIndicator) -> null);

		completableFuture.get(300, TimeUnit.MILLISECONDS);

		assertThat(requestManager.removableRequestList.size(), equalTo(0));
	}

	@Test
	void testWriteRequestsAreClearedFromRequestList()
			throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Object> completableFuture = requestManager.runWrite(() -> null, (a, b) -> null);

		completableFuture.get(300, TimeUnit.MILLISECONDS);

		assertThat(requestManager.removableRequestList.size(), equalTo(0));
	}

    @Test
    void testReadRequestIsCancelledWhenWriteRequestIsQueued() throws ExecutionException, InterruptedException, TimeoutException {
        CountDownLatch started = new CountDownLatch(1);

        CompletableFuture<Object> readFuture = requestManager.runRead((cancelIndicator) -> {
            started.countDown();
            return busyWait(cancelIndicator);
        });
        assertThat(started.await(1, TimeUnit.SECONDS), equalTo(true));
        
        CompletableFuture<Object> writeFuture = requestManager.runWrite(() -> null, (a, b) -> null);
        writeFuture.get(5, TimeUnit.SECONDS);

        ExecutionException ex = Assertions.assertThrows(ExecutionException.class, () -> readFuture.get(5, TimeUnit.SECONDS));
        assertThat(ex.getCause(), instanceOf(CancellationException.class));
        assertThat(readFuture.isCancelled(), equalTo(true));
    }
    
    private <T> T busyWait(CancelIndicator cancelIndicator) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            System.out.println("busy wait");
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException("Read was cancelled");
            }
            // small pause to yield
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2));
        }
        return null;

    }
}