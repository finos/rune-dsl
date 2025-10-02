package test.escaping.importoverlap.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.RosettaFunction;


@ImplementedBy(MyFunc.MyFuncDefault.class)
public abstract class MyFunc implements RosettaFunction {

	/**
	* @return result 
	*/
	public Integer evaluate() {
		Integer result = doEvaluate();
		
		return result;
	}

	protected abstract Integer doEvaluate();

	public static class MyFuncDefault extends MyFunc {
		@Override
		protected Integer doEvaluate() {
			Integer result = null;
			return assignOutput(result);
		}
		
		protected Integer assignOutput(Integer result) {
			return result;
		}
	}
}
