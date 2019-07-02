package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

public class Filter<I, K extends Comparable<K>> extends NamedNode implements ProcessorNode<I, I, K> {

	Predicate<? super I> filter;	
	
	public Filter(String uri, String label, Predicate<? super I> filter) {
		super(uri, label);
		this.filter = filter;
	}

	@Override
	public <T extends I> Optional<GroupableData<I, K>> process(GroupableData<T, K> input) {
		if (filter.test(input.getData())) {
			return Optional.of(input.withIssues(input.getData(), Collections.emptyList(), this));
		}
		return Optional.empty();
	}

}
