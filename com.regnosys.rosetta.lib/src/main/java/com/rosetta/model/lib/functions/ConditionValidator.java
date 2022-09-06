package com.rosetta.model.lib.functions;

import java.util.function.Supplier;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.ComparisonResult;

@ImplementedBy(DefaultConditionValidator.class)
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
		private static final long serialVersionUID = 1L;

        public ConditionException(String message) {
            super(message);
        }

        public ConditionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
