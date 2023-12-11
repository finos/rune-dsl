package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

public class ValidationResult{

	private boolean success;

	private Optional<String> failureReason;

	private RosettaPath path;

	public ValidationResult(boolean success, Optional<String> failureReason, RosettaPath path, Optional<ValidationData> data) {
		this.success = success;
		this.failureReason = failureReason;
		this.path = path;
		this.data = data;
	}

	private Optional<ValidationData> data;

	static ValidationResult success(boolean success, RosettaPath path) {
		return new ValidationResult(true,Optional.empty(), path, Optional.empty());
	}
	
	static ValidationResult failure(boolean success, RosettaPath path, String failureReason, ValidationData data) {
		return new ValidationResult(false,Optional.of(failureReason), path, Optional.of(data));
	}

}
