package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;

public interface Validator<T extends RosettaModelObject> {
	ValidationResult<T> validate(RosettaPath path, T objectToBeValidated);
	ValidationResult<T> validate(RosettaPath path, RosettaModelObjectBuilder objectToBeValidated);
}