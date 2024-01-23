package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

public class ElementValidationResult{
	private boolean success;
	private Optional<String> failureReason;
	private RosettaPath path;
	private Optional<ValidationData> data;

	public ElementValidationResult(boolean success, String failureReason, RosettaPath path, ValidationData data) {
		this.success = success;
		this.failureReason = failureReason!=""?Optional.ofNullable(failureReason):Optional.empty();
		this.path = path;
		this.data = data!=null?Optional.ofNullable(data):Optional.empty();
	}

	public static ElementValidationResult success(RosettaPath path) {
		return new ElementValidationResult(true, "", path, null);
	}
	
	public static ElementValidationResult failure(RosettaPath path, String failureReason, ValidationData data) {
		return new ElementValidationResult(false, failureReason, path, data);
	}

	public boolean isSuccess() {
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
