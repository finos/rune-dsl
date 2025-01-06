package com.regnosys.rosetta.tests;

import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper.Mode;

import com.google.inject.Binder;
import com.regnosys.rosetta.RosettaRuntimeModule;

public class RosettaTestInjectorProvider extends RosettaInjectorProvider {
	protected RosettaRuntimeModule createRuntimeModule() {
		// make it work also with Maven/Tycho and OSGI
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
		return new RosettaRuntimeModule() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return RosettaTestInjectorProvider.class
						.getClassLoader();
			}
			
			// Make sure validation tests properly check the message
			public void configureValidationTestHelper(Binder binder) {
				binder.bind(ValidationTestHelper.class).toInstance(new ValidationTestHelper(Mode.EXACT));
			}
		};
	}
}
