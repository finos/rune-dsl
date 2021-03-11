package com.regnosys.rosetta.blueprints.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UpstreamList<I, K> {
	AtomicInteger count = new AtomicInteger(0);
	
	List<Upstream<? extends I,K>> up = new ArrayList<>();
	
	public void addUpstream(Upstream<? extends I,K> upstream) {
		up.add(upstream);
		count.incrementAndGet();
	}
	
	public List<Upstream<? extends I,K>> getUpstreams() {
		return up;
	}
	
	public boolean allTerminated() {
		return count.get()==0;
	}
	
	public boolean terminateUpstream() {
		return count.decrementAndGet() == 0;
	}
}
