package com.rosetta.model.lib.functions;

import java.util.List;
import java.util.function.Supplier;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.expression.ComparisonResult;

public interface FunctionValidator {

	void validateCondition(Supplier<ComparisonResult> condition, String description);
	
	<T extends RosettaModelObject> void validate(Class<T> clazz, T object);

	<T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects);
}
