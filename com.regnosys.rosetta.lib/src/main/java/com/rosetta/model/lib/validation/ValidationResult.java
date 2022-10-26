package com.rosetta.model.lib.validation;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.function.Function;

import static com.rosetta.model.lib.validation.ValidationResult.ValidationType.CHOICE_RULE;

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
		DATA_RULE, CHOICE_RULE, MODEL_INSTANCE, ONLY_EXISTS, POST_PROCESS_EXCEPTION
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
			if (!failureReason.isEmpty() && modelObjectName.endsWith("Report") && failureReason.get().contains(modelObjectName)) {
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

		private Optional<String> getUpdatedFailureReason() {

			String conditionName = name.replaceFirst(modelObjectName, "");
			String failReason = failureReason.get();

			failReason = failReason.replaceAll(modelObjectName, "");
			failReason = failReason.replaceAll("->get", " ");
			failReason = failReason.replaceAll("[^\\w-]+", " ");

			return Optional.of(conditionName + ":- " + failReason);
		}
	}

	class ChoiceRuleFailure<T> implements ValidationResult<T> {

		private final String name;
		private final String modelObjectName;
		private final List<String> populatedFields;
		private final List<String> choiceFieldNames;
		private final ChoiceRuleValidationMethod validationMethod;
		private final RosettaPath path;

		public ChoiceRuleFailure(String name, String modelObjectName, List<String> choiceFieldNames, RosettaPath path, List<String> populatedFields,
								 ChoiceRuleValidationMethod validationMethod) {
			this.name = name;
			this.path = path;
			this.modelObjectName = modelObjectName;
			this.populatedFields = populatedFields;
			this.choiceFieldNames = choiceFieldNames;
			this.validationMethod = validationMethod;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public String getName() {
			return name;
		}
		
		public RosettaPath getPath() {
			return path;
		}

		@Override
		public String getModelObjectName() {
			return modelObjectName;
		}

		public List<String> populatedFields() {
			return populatedFields;
		}

		public List<String> choiceFieldNames() {
			return choiceFieldNames;
		}

		public ChoiceRuleValidationMethod validationMethod() {
			return validationMethod;
		}

		@Override
		public String getDefinition() {
			return choiceFieldNames.stream()
				.collect(Collectors.joining("', '", validationMethod.desc + " of '", "'. "));
		}
		
		@Override
		public Optional<String> getFailureReason() {
			return Optional.of(getDefinition() + (populatedFields.isEmpty() ? "No fields are set." :
					populatedFields.stream().collect(Collectors.joining("', '", "Set fields are '", "'."))));
		}

		@Override
		public ValidationType getValidationType() {
			return CHOICE_RULE;
		}

		@Override
		public String toString() {
			return String.format("Validation %s on [%s] for [%s] [%s] %s",
					isSuccess() ? "SUCCESS" : "FAILURE",
					path.buildPath(),
					CHOICE_RULE + ":" + validationMethod,
					name,
					getFailureReason().map(reason -> "because " + reason).orElse(""));
		}
	}

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