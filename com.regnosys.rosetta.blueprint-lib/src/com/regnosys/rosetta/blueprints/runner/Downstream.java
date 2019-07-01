package com.regnosys.rosetta.blueprints.runner;

import java.util.Collection;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public interface Downstream<I, K extends Comparable<K>> extends StreamNode{
	//marks a node consumes I that can be downstream of a node that produces  I
	<I2 extends I> void process(GroupableData<I2, K> input);
	void terminate();
	void addUpstream(Upstream<? extends I, K> upstream);
	Collection<Upstream<? extends I, K>> getUpstreams();
}
