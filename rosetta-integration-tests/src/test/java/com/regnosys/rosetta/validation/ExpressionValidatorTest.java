package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExpressionValidatorTest {
	@Inject
	private ValidationTestHelper validationTestHelper;
	@Inject
	private RosettaTestModelService modelService;
	
	@Test
	void testValidChoiceConstruction() {
		RosettaExpression expr =
			modelService.toTestModel("""
				choice Foo:
					Bar
					Qux
				
				type Bar:
					barAttr int (1..1)
				type Qux:
				""").parseExpression("""
				Foo {
					Bar: Bar {
						barAttr: 42
					},
					...
				}
				""");
		
		validationTestHelper.assertNoIssues(expr);
	}
}
