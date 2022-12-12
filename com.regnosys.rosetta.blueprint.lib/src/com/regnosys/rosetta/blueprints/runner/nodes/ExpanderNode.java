package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;

public interface ExpanderNode<I,O, K> extends Node<I, O, K, K> {
	<T extends I, KO extends K> Collection<GroupableData<? extends O, KO>> process(GroupableData<T, KO> input);
	Collection<GroupableData<? extends O, ? extends K>> terminate();
}
