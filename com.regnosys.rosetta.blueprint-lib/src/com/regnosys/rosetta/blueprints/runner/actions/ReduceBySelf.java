package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public class ReduceBySelf<I extends Comparable<I>, K> extends ReduceParent<I, I, K> {

	public ReduceBySelf(String uri, String label, Action action, DataIdentifier identifier) {
		super(uri, label, action, identifier);
	}

	@Override
	protected <T extends I, K2 extends K> I getReduction(GroupableData<T, K2> input) {
		return input.getData();
	}

}
