package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

public interface ValidatorWithArg<T extends RosettaModelObject, A> {
	<T2 extends T> ModelValidationResult<T> validate(RosettaPath path, T2 objectToBeValidated, A arg);
}
