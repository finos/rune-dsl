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