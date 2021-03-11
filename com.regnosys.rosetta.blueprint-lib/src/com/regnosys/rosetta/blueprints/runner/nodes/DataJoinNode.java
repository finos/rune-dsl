package com.regnosys.rosetta.blueprints.runner.nodes;

import java.util.Collection;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public interface DataJoinNode<I1, I2, K> extends Node<Object, I1, K, K>  {
	Collection<GroupableData<I1, K>> process(GroupableData<?,?> input);
	Collection<GroupableData<I1, K>> terminate();
	String getLabel();
	String getURI();
}
