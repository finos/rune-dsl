package com.regnosys.rosetta.blueprints.runner;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.GroupNode;

import java.util.Collection;
import java.util.Optional;

public class StreamGroup<I, K, K2> 
	extends Upstream<I, K2> 
	implements Downstream<I, K>{
	
	GroupNode<I, K, K2> processor;
	
	UpstreamList<I, K> upstreamList = new UpstreamList<>();
	
	public StreamGroup(GroupNode<I, K, K2> processor) {
		super(processor.getURI(), processor.getName());
		this.processor = processor;
	}

	@Override
	public <I2 extends I, KI extends K> void process(GroupableData<I2, KI> input) {
		Optional<GroupableData<I2, K2>> output = processor.process(input);
		if (output.isPresent()) {
			downstream.distribute(output.get());
		}
	}

	@Override
	public void terminate() {
		if (upstreamList.terminateUpstream()) {
			Collection<GroupableData<I, K2>> terminal = processor.terminate();
			for (GroupableData<I, K2> data:terminal) {
				downstream.distribute(data);
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
