package com.rosetta.model.lib.validation;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

public interface Validator<T extends RosettaModelObject> {

	@Deprecated // Since 9.7.0: use `getValidationResults` instead.
	ValidationResult<T> validate(RosettaPath path, T objectToBeValidated);
	
	default List<ValidationResult<?>> getValidationResults(RosettaPath path, T objectToBeValidated) {
		return null; // @Compat: for backwards compatibility. Old generated code will not have an implementation for this method.
	}
}