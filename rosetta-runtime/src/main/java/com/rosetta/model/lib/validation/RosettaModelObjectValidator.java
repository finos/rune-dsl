package com.rosetta.model.lib.validation;
import com.rosetta.model.lib.path.RosettaPath;


public interface RosettaModelObjectValidator<T> {

   TypeValidation validate(RosettaPath path, T instance);

}
