package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

public interface ValidationResult<T> {

	boolean isSuccess();

	@Deprecated
	String getModelObjectName();

	@Deprecated
	String getName();

	@Deprecated
	ValidationType getValidationType();
	@Deprecated
	String getDefinition();
	
	Optional<String> getFailureReason();
	
	RosettaPath getPath();

	Optional<ValidationData> getData();

	static <T> ValidationResult<T> success(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition) {
		return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.empty(), Optional.empty());
	}
	
	static <T> ValidationResult<T> failure(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, String failureMessage) {
		return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.of(failureMessage), Optional.empty());
	}

}
