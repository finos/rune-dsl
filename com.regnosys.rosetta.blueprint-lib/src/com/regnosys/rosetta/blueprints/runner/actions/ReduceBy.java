package com.regnosys.rosetta.blueprints.runner.actions;

import java.util.function.Function;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public class ReduceBy<I, Kr extends Comparable<Kr> , K> extends ReduceParent<I, Kr, K>  {

	private Function<? super I, Kr> evalFunc;
	
	public ReduceBy(String uri, String label, Action action, Function<I, Kr> evalFunc) {
		super(uri,label, action);
		this.evalFunc = evalFunc;
	}

	@Override
	protected <T extends I, K2 extends K> Kr getReduction(GroupableData<T,K2> input) {
		Kr key = evalFunc.apply(input.getData());
		return key;
	}

}
