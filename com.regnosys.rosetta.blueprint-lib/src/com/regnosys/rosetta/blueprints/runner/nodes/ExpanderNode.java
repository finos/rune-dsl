package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;

public interface ExpanderNode<I,O, K> extends Node<I, O, K, K> {
	Collection<GroupableData<O, K>> process(GroupableData<? extends I, ? extends K> input);
	Collection<GroupableData<? extends O, K>> terminate();
	String getName();
	String getURI();
}
