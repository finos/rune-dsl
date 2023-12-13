package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

public class ValidationResult<T>{
	private static boolean success;
	private Optional<String> failureReason;
	private RosettaPath path;
	private Optional<ValidationData> data;

	@SuppressWarnings("static-access")
	public ValidationResult(boolean success, Optional<String> failureReason, RosettaPath path, Optional<ValidationData> data) {
		this.success = success;
		this.failureReason = failureReason;
		this.path = path;
		this.data = data;
	}

	static <T> ValidationResult<T> success(boolean success, RosettaPath path) {
		return new ValidationResult<T>(true,Optional.empty(), path, Optional.empty());
	}
	
	static <T> ValidationResult<T>failure(boolean success, RosettaPath path, String failureReason, ValidationData data) {
		return new ValidationResult<T>(false,Optional.of(failureReason), path, Optional.of(data));
	}

	public static boolean isSuccess() {
		return success;
	}

	public Optional<String> getFailureReason() {
		return failureReason;
	}

	public RosettaPath getPath() {
		return path;
	}

	public Optional<ValidationData> getData() {
		return data;
	}
}
