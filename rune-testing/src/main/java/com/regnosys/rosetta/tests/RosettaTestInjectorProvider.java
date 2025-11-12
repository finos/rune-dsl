package com.regnosys.rosetta.tests;

import com.google.inject.Module;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;

import com.google.inject.Binder;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import com.rosetta.model.lib.validation.ValidatorFactory;

public class RosettaTestInjectorProvider extends RosettaInjectorProvider {
	@Override
    protected Module createRuntimeModule() {
		// make it work also with Maven/Tycho and OSGI
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
		return new RosettaRuntimeModule() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return RosettaTestInjectorProvider.class
						.getClassLoader();
			}
			
			public void configureValidationTestHelper(Binder binder) {
				binder.bind(ValidationTestHelper.class).to(RosettaValidationTestHelper.class);
			}
			
			public Class<? extends ValidatorFactory> bindValidatorFactory() {
				return ValidatorFactory.Default.class;
			}
		};
	}
}
