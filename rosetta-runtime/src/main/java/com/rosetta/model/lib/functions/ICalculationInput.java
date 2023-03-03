package com.rosetta.model.lib.functions;

import java.util.Collections;
import java.util.List;

public interface ICalculationInput extends IResult {

	/**
	 * @return String that represent the formula to be calculated.
	 */
	List<Formula> getFormulas();

	/**
	 * @return dependent calculation results of this input.
	 */
	default List<ICalculationResult> getCalculationResults() {
		return Collections.emptyList();
	}
}