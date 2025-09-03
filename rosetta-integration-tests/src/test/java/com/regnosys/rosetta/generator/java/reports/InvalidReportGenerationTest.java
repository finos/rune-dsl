package com.regnosys.rosetta.generator.java.reports;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class InvalidReportGenerationTest {
	@Inject
	private RosettaTestModelService modelService;
	@Inject
	private RosettaValidationTestHelper validationTestHelper;
	@Inject
    private CodeGeneratorTestHelper generatorTestHelper;
	
	@Test
	void invalidRuleReferenceShouldNotThrowGenerationException() {
		var model = modelService.toTestModel(
				"""
				namespace demo.namespace2

				type Foo:
					attr string (1..1)
						[ruleReference FooAttr]
				""",
				false,
				"""
				namespace demo.namespace1

				import demo.namespace2.*

				body Authority Reg
				corpus Reg Trade

				report Reg Trade in T+1
					from int
					when EligibRule
					with type Foo

				eligibility rule EligibRule from int:
				    True
				""");
		
		// Assert that we have an invalid rule reference (FooAttr does not exist)
		validationTestHelper.assertIssues(model.getModel(), """
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaRule 'FooAttr'.' at 5:18, length 7, on RuleReferenceAnnotation
				""");
		
		Assertions.assertDoesNotThrow(() -> generatorTestHelper.generateCode(model.getResourceSet().getResources()));
	}
}
