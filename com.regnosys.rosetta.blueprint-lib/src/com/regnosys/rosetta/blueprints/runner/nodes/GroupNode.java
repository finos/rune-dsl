package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;
import java.util.Optional;

public interface GroupNode <I, K extends Comparable<K>, K2 extends Comparable<K2>> extends Node<I, I, K, K2> {
	<I2 extends I> Optional<GroupableData<I2, K2>> process(GroupableData<I2, K> input);
	Collection<GroupableData<I, K2>> terminate();
	String getName();
	String getURI();
}
