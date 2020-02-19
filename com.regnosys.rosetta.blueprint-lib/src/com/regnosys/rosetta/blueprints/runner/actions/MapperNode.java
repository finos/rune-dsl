package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class MapperNode<I, O, K extends Comparable<K>> extends NamedNode implements ProcessorNode<I, O, K> {

	private final Function<I, O> mapFunction;
	private final DataIdentifier identifier;
	
	public MapperNode(String uri, String label, Function<I, O> mapFunction, DataIdentifier identifier) {
		super(uri, label);
		this.mapFunction = mapFunction;
		this.identifier = identifier;
	}



	@Override
	public <T extends I> Optional<GroupableData<O, K>> process(GroupableData<T, K> input) {
		O result = mapFunction.apply(input.getData());
		return Optional.ofNullable(input.withNewData(result, identifier, Collections.emptyList(), this));
	}

}
