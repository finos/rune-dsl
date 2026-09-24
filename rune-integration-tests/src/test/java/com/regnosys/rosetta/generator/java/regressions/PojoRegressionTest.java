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
// The enum fixture pins the enum itself; it is here only to type the enum attributes
@ExpectedFiles(exclude = "test/pojo/PojoEnum.java")
public class PojoRegressionTest extends AbstractJavaGeneratorRegressionTest {
	@Override
	protected String getTestRootResourceFolder() {
		return "generation-regression-tests/pojo";
	}
}
