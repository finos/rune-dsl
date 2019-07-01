package com.regnosys.rosetta.blueprints.runner;

import java.util.Collection;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.DataJoinNode;

public class StreamDataJoin<I1, I2, K extends Comparable<K>> extends Upstream<I1, K> implements Downstream<Object, K> {

	private DataJoinNode<I1, I2, K> joinNode;
	
	UpstreamList<Object, K> upstreamList = new UpstreamList<>();

	public StreamDataJoin(DataJoinNode<I1, I2, K> joinNode) {
		super(joinNode.getURI(), joinNode.getLabel());
		this.joinNode = joinNode;
	}

	@Override
	public <IU> void process(GroupableData<IU, K> input) {
		Collection<GroupableData<I1, K>> outputCol = joinNode.process(input);
		for (GroupableData<I1, K> output:outputCol) {
			downstream.distribute(output);
		}
	}

	@Override
	public void terminate() {

		if (upstreamList.terminateUpstream()) {
			Collection<GroupableData<I1, K>> outputCol = joinNode.terminate();
			for (GroupableData<I1, K> output:outputCol) {
				downstream.distribute(output);
			}	
			downstream.terminate();
		}
	}

	@Override
	public void addUpstream(Upstream<? extends Object, K> upstream) {
		upstreamList.addUpstream(upstream);
		
	}

	@Override
	public Collection<Upstream<? extends Object, K>> getUpstreams() {
		return upstreamList.getUpstreams();
	}

}
