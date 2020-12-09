package com.rosetta.model.lib.validation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.rosetta.model.lib.RosettaModelObject;

public interface ValidatorFactory {

	<T extends RosettaModelObject> Validator<? super T> create(Class<? extends Validator<T>> clazz);

	public static class Default implements ValidatorFactory {

		@Inject
		Injector injector;

		@Override
		public <T extends RosettaModelObject> Validator<? super T> create(Class<? extends Validator<T>> clazz) {
			return injector.getInstance(clazz);
		}
	}
}
