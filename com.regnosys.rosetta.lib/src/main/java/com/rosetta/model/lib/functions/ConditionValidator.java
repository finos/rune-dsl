package com.rosetta.model.lib.functions;

import java.util.function.Supplier;

import com.rosetta.model.lib.expression.ComparisonResult;

public interface ConditionValidator {

	/**
	 * Evaluates conditions. Implementation may throw an exception if condition fails.
	 * 
	 * @param condition
	 * @param description
	 * @throws ConditionException if condition fails
	 */
    void validate(Supplier<ComparisonResult> condition, String description);


    class ConditionException extends RuntimeException {

        public ConditionException(String message) {
            super(message);
        }

        public ConditionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
