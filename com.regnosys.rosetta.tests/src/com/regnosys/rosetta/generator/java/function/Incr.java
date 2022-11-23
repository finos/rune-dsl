package com.regnosys.rosetta.generator.java.function;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;


@ImplementedBy(Incr.IncrDefault.class)
public abstract class Incr implements RosettaFunction {

	/**
	* @param a 
	* @return result 
	*/
	public Integer evaluate(Integer a) {
		Integer result = doEvaluate(a);
		
		return result;
	}

	protected abstract Integer doEvaluate(Integer a);

	public static class IncrDefault extends Incr {
		@Override
		protected Integer doEvaluate(Integer a) {
			Integer result = null;
			return assignOutput(result, a);
		}
		
		protected Integer assignOutput(Integer result, Integer a) {
			result = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(a), MapperS.of(Integer.valueOf(1))).get();
			
			return result;
		}
	}
}

