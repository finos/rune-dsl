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

import java.util.Optional;
import java.util.function.Function;

import com.rosetta.model.lib.path.RosettaPath;

public interface ValidationResult<T> {

	boolean isSuccess();

	String getModelObjectName();

	String getName();
	
	ValidationType getValidationType();

	String getDefinition();
	
	Optional<String> getFailureReason();
	
	RosettaPath getPath();

	static <T> ValidationResult<T> success(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition) {
		return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.empty());
	}
	
	static <T> ValidationResult<T> failure(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, String failureMessage) {
		return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.of(failureMessage));
	}

	enum ValidationType {
		DATA_RULE, CARDINALITY, TYPE_FORMAT, KEY, ONLY_EXISTS, PRE_PROCESS_EXCEPTION, POST_PROCESS_EXCEPTION
	}

	class ModelValidationResult<T> implements ValidationResult<T> {

		private final String modelObjectName;
		private final String name;
		private final String definition;
		private final Optional<String> failureReason;
		private final ValidationType validationType;
		private final RosettaPath path;

		public ModelValidationResult(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, Optional<String> failureReason) {
			this.name = name;
			this.validationType = validationType;
			this.path = path;
			this.modelObjectName = modelObjectName;
			this.definition = definition;
			this.failureReason = failureReason;
		}

		@Override
		public boolean isSuccess() {
			return !failureReason.isPresent();
		}

		@Override
		public String getModelObjectName() {
			return modelObjectName;
		}

		@Override
		public String getName() {
			return name;
		}
		
		public RosettaPath getPath() {
			return path;
		}

		@Override
		public String getDefinition() {
			return definition;
		}
		
		@Override
		public Optional<String> getFailureReason() {
			if (failureReason.isPresent() && modelObjectName.endsWith("Report") && ValidationType.DATA_RULE.equals(validationType)) {
				return getUpdatedFailureReason();
			}
			return failureReason;
		}

		@Override
		public ValidationType getValidationType() {
			return validationType;
		}

		@Override
		public String toString() {
			return String.format("Validation %s on [%s] for [%s] [%s] %s",
					isSuccess() ? "SUCCESS" : "FAILURE",
					path.buildPath(),
					validationType,
					name,
					failureReason.map(s -> "because [" + s + "]").orElse(""));
		}

		// TODO: refactor this method. This is an ugly hack.
		private Optional<String> getUpdatedFailureReason() {

			String conditionName = name.replaceFirst(modelObjectName, "");
			String failReason = failureReason.get();

			failReason = failReason.replaceAll(modelObjectName, "");
			failReason = failReason.replaceAll("->get", " ");
			failReason = failReason.replaceAll("[^\\w-]+", " ");
			failReason = failReason.replaceAll("^\\s+", "");

			return Optional.of(conditionName + ":- " + failReason);
		}
	}
	
	@Deprecated // Since 9.7.0
	enum ChoiceRuleValidationMethod {

		OPTIONAL("Zero or one field must be set", fieldCount -> fieldCount == 1 || fieldCount == 0),
		REQUIRED("One and only one field must be set", fieldCount -> fieldCount == 1);

		private final String desc;
		private final Function<Integer, Boolean> check;

		ChoiceRuleValidationMethod(String desc, Function<Integer, Boolean> check) {
			this.desc = desc;
			this.check = check;
		}

		public boolean check(int fields) {
			return check.apply(fields);
		}
		
		public String getDescription() {
			return this.desc;
		}
	}
	
	class ProcessValidationResult<T> implements ValidationResult<T> {
		private String message;
		private String modelObjectName;
		private String processorName;
		private RosettaPath path;

		public ProcessValidationResult(String message, String modelObjectName, String processorName, RosettaPath path) {
			this.message = message;
			this.modelObjectName = modelObjectName;
			this.processorName = processorName;
			this.path = path;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public String getModelObjectName() {
			return modelObjectName;
		}

		@Override
		public String getName() {
			return processorName;
		}

		@Override
		public ValidationType getValidationType() {
			return ValidationType.POST_PROCESS_EXCEPTION;
		}

		@Override
		public String getDefinition() {
			return "";
		}

		@Override
		public Optional<String> getFailureReason() {
			return Optional.of(message);
		}

		@Override
		public RosettaPath getPath() {
			return path;
		}
	}
}
