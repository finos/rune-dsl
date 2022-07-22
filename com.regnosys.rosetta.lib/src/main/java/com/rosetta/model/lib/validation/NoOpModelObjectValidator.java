package com.rosetta.model.lib.validation;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;

public class NoOpModelObjectValidator implements ModelObjectValidator {

	@Override
	public <T extends RosettaModelObject> void validate(Class<T> clazz, T object) {
		// do nothing
	}

	@Override
	public <T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects) {
		// do nothing
	}

}
