package com.rosetta.model.lib.validation;

import java.util.Collections;
import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

public interface Validator<T extends RosettaModelObject> {

	ValidationResult<T> validate(RosettaPath path, T objectToBeValidated);
	
	default List<ValidationResult<?>> getValidationResults(RosettaPath path, T objectToBeValidated) {
		return Collections.emptyList();
	}
}