/*
 * Copyright (c) 2022. REGnosys LTD
 * All rights reserved.
 */

package com.regnosys.rosetta.blueprints.runner.nodes;

import java.util.concurrent.*;

class FutureResult<R> implements Future<R> {

	R result;
	private CountDownLatch isDone = new CountDownLatch(1);
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return isDone.getCount()>0;
	}
	
	public void postResult(R result) {
		this.result = result;
		isDone.countDown();
	}

	@Override
	public R get() throws InterruptedException, ExecutionException {
		isDone.await();
		return result;
	}

	@Override
	public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		isDone.await(timeout, unit);
		return result;
	}

}
