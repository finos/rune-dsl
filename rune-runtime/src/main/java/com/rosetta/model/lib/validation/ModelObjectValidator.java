/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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