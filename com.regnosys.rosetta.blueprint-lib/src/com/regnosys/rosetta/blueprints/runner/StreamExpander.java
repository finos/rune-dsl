package com.regnosys.rosetta.blueprints.runner;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.ExpanderNode;

import java.util.Collection;

public class StreamExpander<I,O, K> extends Upstream<O, K> implements Downstream<I, K> {

	
	ExpanderNode<? super I, O, K> expander;
	
	UpstreamList<I, K> upstreamList = new UpstreamList<>();

	public StreamExpander(ExpanderNode<? super I, O, K> expander) {
		super(expander.getURI(), expander.getName());
		this.expander = expander;
	}
	
	@Override
	public <I2 extends I, K2 extends K> void process(GroupableData<I2,K2> input) {
		Collection<GroupableData<O, K>> outputCol = expander.process(input);
		for (GroupableData<O, K> output:outputCol) {
			downstream.distribute(output);
		}
	}

	@Override
	public void terminate() {
		if (upstreamList.terminateUpstream()) {
			Collection<GroupableData<? extends O, K>> outputCol = expander.terminate();
			for (GroupableData<? extends O, K> output:outputCol) {
				downstream.distribute(output);
			}
			downstream.terminate();
		}	
	}

	@Override
	public void addUpstream(Upstream<? extends I, K> upstream) {
		upstreamList.addUpstream(upstream);
	}

	@Override
	public Collection<Upstream<? extends I, K>> getUpstreams() {
		return upstreamList.getUpstreams();
	}

}
