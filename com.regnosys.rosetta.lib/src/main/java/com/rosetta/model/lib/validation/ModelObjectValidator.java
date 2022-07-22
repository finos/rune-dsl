package com.rosetta.model.lib.validation;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;

public interface ModelObjectValidator {

	/**
	 * Runs validation and collects errors. Implementation may throw an exception if validation fails.
	 * 
	 * @param <T>
	 * @param clazz
	 * @param object
	 * @throws ModelObjectValidationException if validation fails
	 */
	<T extends RosettaModelObject> void validate(Class<T> clazz, T object);

	/**
	 * Runs validation and collects errors. Implementation may throw an exception if validation fails.
	 * 
	 * @param <T>
	 * @param clazz
	 * @param objects
	 * @throws ModelObjectValidationException if validation fails
	 */
	<T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects);
	
	
	class ModelObjectValidationException extends RuntimeException {
		
		private final String errors;

		public ModelObjectValidationException(String errors) {
			super(errors);
			this.errors = errors;
		}

		public String getErrors() {
			return errors;
		}
	}
}