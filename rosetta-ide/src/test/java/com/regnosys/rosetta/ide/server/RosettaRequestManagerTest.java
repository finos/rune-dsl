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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
class RosettaRequestManagerTest {

	@Inject
	RosettaRequestManager rosettaRequestManager;

	@Test
	public void testReadRequestsAreClearedFromRequestList()
			throws ExecutionException, InterruptedException, TimeoutException {

		CompletableFuture<Object> completableFuture = rosettaRequestManager.runRead((cancelIndicator) -> null);

		completableFuture.get(300, TimeUnit.MILLISECONDS);

		assertThat(rosettaRequestManager.removableRequestList.size(), equalTo(0));
	}

	@Test
	public void testWriteRequestsAreClearedFromRequestList()
			throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Object> completableFuture = rosettaRequestManager.runWrite(() -> null, (a, b) -> null);

		completableFuture.get(300, TimeUnit.MILLISECONDS);

		assertThat(rosettaRequestManager.removableRequestList.size(), equalTo(0));
	}

}