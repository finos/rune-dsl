package test.escaping.importoverlap.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;
import javax.inject.Inject;
import test.escaping.importoverlap.dep.functions.MyFunc;


@ImplementedBy(UseFunctions.UseFunctionsDefault.class)
public abstract class UseFunctions implements RosettaFunction {
	
	// RosettaFunction dependencies
	//
	@Inject protected MyFunc myFunc0;
	@Inject protected test.escaping.importoverlap.functions.MyFunc myFunc1;

	/**
	* @return result 
	*/
	public Integer evaluate() {
		Integer result = doEvaluate();
		
		return result;
	}

	protected abstract Integer doEvaluate();

	public static class UseFunctionsDefault extends UseFunctions {
		@Override
		protected Integer doEvaluate() {
			Integer result = null;
			return assignOutput(result);
		}
		
		protected Integer assignOutput(Integer result) {
			result = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(myFunc1.evaluate()), MapperS.of(myFunc0.evaluate())).get();
			
			return result;
		}
	}
}
