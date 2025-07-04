package com.regnosys.rosetta.generator.java.regressions;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(Lifecycle.PER_CLASS)
public class JavaNameEscapingTest extends AbstractJavaGeneratorRegressionTest {
	
	// TODO: also check source code to make sure these tests keep testing what they should test.
	
	// TODO: add test for complex attribute named `result` and multicardinality (complex?) `index` and (complex?) `meta`
	
	@Override
	protected String getTestRootResourceFolder() {
		return "generation-regression-tests/name-escaping";
	}
}
