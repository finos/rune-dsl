package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

public class ValidationResult<T>{
	private static boolean success;
	private Optional<String> failureReason;
	private RosettaPath path;
	private Optional<ValidationData> data;

	@SuppressWarnings("static-access")
	public ValidationResult(boolean success, String failureReason, RosettaPath path, ValidationData data) {
		this.success = success;
		this.failureReason = Optional.ofNullable(failureReason);
		this.path = path;
		this.data = Optional.ofNullable(data);
	}

	public static <T> ValidationResult<T> success(RosettaPath path) {
		return new ValidationResult<T>(true, "", path, null);
	}
	
	public static <T> ValidationResult<T>failure(RosettaPath path, String failureReason, ValidationData data) {
		return new ValidationResult<T>(false, failureReason, path, data);
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

	public static void getValidationType() {
	  throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
}
