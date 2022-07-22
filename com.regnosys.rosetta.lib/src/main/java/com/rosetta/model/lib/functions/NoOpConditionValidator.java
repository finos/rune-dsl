package com.rosetta.model.lib.functions;

import java.util.function.Supplier;

import com.rosetta.model.lib.expression.ComparisonResult;

public class NoOpConditionValidator implements ConditionValidator {

	@Override
	public void validate(Supplier<ComparisonResult> condition, String description) {
		// do nothing
	}
}
