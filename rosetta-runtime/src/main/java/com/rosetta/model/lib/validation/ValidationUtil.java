package com.rosetta.model.lib.validation;

public class ValidationUtil {
	
	public ValidationResult checkCardinality(String msgPrefix, int min, int max, int actual) {
		
		CardinalityValidationData cardinalityValidationData = new CardinalityValidationData(min, max, actual);
		String failureMessage = "";
		if (actual < min) {
			if(actual == 0){
				failureMessage = "'" + msgPrefix + "' is a required field but does not exist."; 
				return ValidationResult
						.failure(true, null, failureMessage, cardinalityValidationData);
			}
			else {
				failureMessage = "Minimum of " + min + " '" + msgPrefix + "' is expected but found " + actual + ".";
				return ValidationResult
						.failure(true, null, failureMessage, cardinalityValidationData);
			}
		} else if (max > 0 && actual > max) {
			failureMessage = "Maximum of " + max + " '" + msgPrefix + "' are expected but found " + actual + ".";
			return ValidationResult
					.failure(true, null, failureMessage, cardinalityValidationData);
		}
		return ValidationResult.success(true, null);
	
		
	}

}
