package com.regnosys.rosetta.generator.java.function;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import java.util.ArrayList;
import java.util.List;


@ImplementedBy(F1.F1Default.class)
public abstract class F1 implements RosettaFunction {
	
	// RosettaFunction dependencies
	//
	@Inject protected Incr incr;

	/**
	* @param list 
	* @return res 
	*/
	public List<Integer> evaluate(List<Integer> list) {
		List<Integer> res = doEvaluate(list);
		
		return res;
	}

	protected abstract List<Integer> doEvaluate(List<Integer> list);

	public static class F1Default extends F1 {
		@Override
		protected List<Integer> doEvaluate(List<Integer> list) {
			List<Integer> res = new ArrayList<>();
			return assignOutput(res, list);
		}
		
		protected List<Integer> assignOutput(List<Integer> res, List<Integer> list) {
			List<Integer> __addVar0 = MapperC.of(list)
				.mapItem((__a) -> MapperS.of(incr.evaluate(__a.get()))).getMulti();
			res.addAll(__addVar0);
			
			return res;
		}
	}
}

