package com.regnosys.rosetta.types;

import javax.inject.Inject;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaTypeProviderTest {
	@Inject
	private RosettaTestModelService modelService;
	@Inject
	private ValidationTestHelper validationTestHelper;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject 
	private RBuiltinTypeService builtins;
	
	private void assertIsValidWithType(RosettaExpression expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		assertIsValidWithType(expr, NodeModelUtils.findActualNodeFor(expr).getText(), expectedType, expectedIsMulti);
	}
	private void assertIsValidWithType(RosettaExpression expr, CharSequence originalExpression, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		validationTestHelper.assertNoIssues(expr);
		RMetaAnnotatedType actualType = typeProvider.getRMetaAnnotatedType(expr);
		boolean actualIsMulti = cardinalityProvider.isMulti(expr);
		
		Assertions.assertAll(
				() -> Assertions.assertEquals(expectedType, actualType, "Expression: " + originalExpression),
				() -> {
					if (expectedIsMulti) {
						Assertions.assertTrue(actualIsMulti, "Expected multi cardinality. Expression: " + originalExpression);
					} else {
						Assertions.assertFalse(actualIsMulti, "Expected single cardinality. Expression: " + originalExpression);
					}
				}
			);
	}
	
	@Test
	void testRecursiveRuleTypeInference() {
		RosettaTestModel model = modelService.toTestModel("""
				namespace test
				
				reporting rule InfiniteRecursion from int:
					InfiniteRecursion
				""");
		RosettaExpression expr = model.parseExpression("InfiniteRecursion(42)");
		
		assertIsValidWithType(expr, builtins.NOTHING_WITH_ANY_META, false);
	}
}
