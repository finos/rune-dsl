package com.regnosys.rosetta.blueprints.runner;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.ArrayList;
import java.util.List;

public class DownstreamList<I, K> {
	protected List<Downstream<? super I, K>> downstream;
	
	public DownstreamList() {
		downstream = new ArrayList<>();
	}
	
	public void distribute(GroupableData<? extends I, ? extends K> input) {
		for (Downstream<? super I, K> down:downstream) {
			down.process(input);
		}
	}
	
	public void terminate() {
		for (Downstream<? super I, K> down:downstream) {
			down.terminate();
		}
	}
	
	public void addDownstream(Downstream<? super I, K> d) {
		downstream.add(d);
	}

	public List<StreamNode> getNodes() {
		return ImmutableList.copyOf(downstream);
	}

}
