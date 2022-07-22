package com.rosetta.model.lib.functions;

import java.util.List;
import java.util.function.Supplier;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.expression.ComparisonResult;

public class NoOpFunctionValidator implements FunctionValidator {

	@Override
	public void validateCondition(Supplier<ComparisonResult> condition, String description) {
		// do nothing
	}

	@Override
	public <T extends RosettaModelObject> void validate(Class<T> clazz, T object) {
		// do nothing
	}

	@Override
	public <T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects) {
		// do nothing
	}

}
