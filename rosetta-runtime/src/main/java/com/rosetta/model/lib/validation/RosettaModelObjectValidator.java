package com.rosetta.model.lib.validation;

public interface RosettaModelObjectValidator<T> {

   TypeValidation validate(T instance);

}
