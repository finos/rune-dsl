package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Optional;

public interface SourceNode<O, K extends Comparable<K>> extends Node<Void, O, K, K> {
	Optional<GroupableData<O, K>> nextItem();
	String getName();
	String getURI();

}
