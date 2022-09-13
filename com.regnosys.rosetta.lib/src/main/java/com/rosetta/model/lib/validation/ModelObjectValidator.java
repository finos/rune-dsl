package com.rosetta.model.lib.validation;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;

/**
 * Deprecated - use version in package com.rosetta.model.lib.functions instead.
 * 
 * @see com.rosetta.model.lib.functions.ModelObjectValidator
 */
@Deprecated
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
	<T extends RosettaModelObject> void validateAndFailOnErorr(Class<T> topClass, List<? extends T> modelObjects);

	class ModelObjectValidationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
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