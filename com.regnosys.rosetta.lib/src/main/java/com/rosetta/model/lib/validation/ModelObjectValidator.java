package com.rosetta.model.lib.validation;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;

public interface ModelObjectValidator {

	/**
	 * Runs validation and collects errors. Throws an exception if validation fails
	 * 
	 * @param <T>
	 * @param topClass
	 * @param modelObject
	 * @throws RuntimeException if validation fails
	 */
	<T extends RosettaModelObject> void validateAndFailOnErorr(Class<T> topClass, T modelObject);

	/**
	 * Runs validation and collects errors. Throws an exception if validation fails
	 * 
	 * @param <T>
	 * @param topClass
	 * @param modelObjects
	 * @throws RuntimeException if validation fails
	 */
	<T extends RosettaModelObject> void validateAndFailOnErorr(Class<T> topClass, List<T> modelObjects);
	
	class ModelObjectValidationException extends RuntimeException {
		private final String errors;

		public ModelObjectValidationException(String errors) {
			super();
			this.errors = errors;
		}

		public String getErrors() {
			return errors;
		}
	}

}