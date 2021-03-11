package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.BlueprintInstance;
import com.regnosys.rosetta.blueprints.runner.CaptureDownstream;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import java.util.List;

public class ReduceByRule<I, Kr extends Comparable<Kr> , K> extends ReduceParent<I, Kr, K> {

	
	BlueprintInstance<? super I, Kr, ? super I, ? super I> evalBP;
	CaptureDownstream<Kr, I> downstream;
	
	public ReduceByRule(String uri, String label, Action action, BlueprintInstance<? super I, Kr, I, I> evalBP) {
		super(uri,label, action);
		this.evalBP = evalBP;
		downstream = new CaptureDownstream<>();
		evalBP.addDownstreams(downstream);
	}
	
	protected <T extends I, K2 extends K> Kr getReduction(GroupableData<T,K2> input) {
		GroupableData<T,T> reKeyed = input.withNewKey(input.getData(), List.of(), this);
		evalBP.process(reKeyed);
		evalBP.terminate();
		GroupableData<? extends Kr, ? extends I> result = downstream.getResult(input.getData());
		return result.getData();
	}
}
