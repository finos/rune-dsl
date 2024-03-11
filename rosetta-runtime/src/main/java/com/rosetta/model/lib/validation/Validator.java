package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult.ValidationType;

public interface Validator<T extends RosettaModelObject> {

	@Deprecated // Since 9.7.0: use `getValidationResults` instead.
	default ValidationResult<T> validate(RosettaPath path, T objectToBeValidated) {
		List<ValidationResult<?>> results = getValidationResults(path, objectToBeValidated);
		String error = results.stream().filter(res -> !res.isSuccess()).map(res -> res.getFailureReason().get()).collect(Collectors.joining("; "));
		ValidationType type = results.stream().map(r -> r.getValidationType()).findAny().orElse(ValidationType.MODEL_INSTANCE);
		
		if (!Strings.isNullOrEmpty(error)) {
			return ValidationResult.failure("Foo", type, "Foo", path, "", error);
		}
		return ValidationResult.success("Foo", type, "Foo", path, "");
	}
	
	default List<ValidationResult<?>> getValidationResults(RosettaPath path, T objectToBeValidated) {
		return null; // @Compat: for backwards compatibility. Old generated code will not have an implementation for this method.
	}
}