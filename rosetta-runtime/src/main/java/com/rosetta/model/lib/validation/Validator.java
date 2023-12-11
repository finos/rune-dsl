package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

public interface Validator<T extends RosettaModelObject> {

	ModelValidationResult<T> validate(RosettaPath path, T objectToBeValidated);
}