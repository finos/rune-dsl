package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import java.util.Optional;

public class IdChange<I,K> extends NamedNode implements ProcessorNode<I, I, K> {

	public IdChange(String uri, String label, DataIdentifier identifier) {
		super(uri, label, identifier);
	}

	@Override
	public <T extends I, KO extends K> Optional<GroupableData<I, KO>> process(GroupableData<T, KO> input) {
		@SuppressWarnings("unchecked")
		GroupableData<I, KO> newData = (GroupableData<I, KO>) input.withNewIdentifier(getIdentifier(), input.getIssues(), this);
		return Optional.of(newData);
	}

}
