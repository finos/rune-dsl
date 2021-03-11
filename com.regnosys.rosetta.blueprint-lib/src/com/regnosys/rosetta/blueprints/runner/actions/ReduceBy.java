package com.regnosys.rosetta.blueprints.runner.actions;

import java.util.function.Function;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

public class ReduceBy<I, Kr extends Comparable<Kr> , K> extends ReduceParent<I, Kr, K>  {

	private Function<? super I, Kr> evalFunc;
	
	public ReduceBy(String uri, String label, Action action, Function<I, Kr> evalFunc) {
		super(uri,label);
		this.action = action;
		this.evalFunc = evalFunc;
	}
	public ReduceBy(String uri, String label, Action action) {
		super(uri,label);
		this.action = action;
		evalFunc = a->null;
	}

	@Override
	protected <T extends I, K2 extends K> Kr getReduction(GroupableData<T,K2> input) {
		Kr key = evalFunc.apply(input.getData());
		return key;
	}

}
