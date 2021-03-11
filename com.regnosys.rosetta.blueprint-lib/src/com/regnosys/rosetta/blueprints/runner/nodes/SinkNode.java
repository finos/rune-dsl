package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.Issue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public interface SinkNode<I, O, K> extends Node<I, O, K, K> {
	void process(GroupableData<? extends I, ? extends K> input);
	void terminate();
	Future<O> result();
	Future<List<Collection<Issue>>> issues();

	Collection<GroupableData<? extends I, K>> getFinalData();
	String getURI();
	String getLabel();
}
