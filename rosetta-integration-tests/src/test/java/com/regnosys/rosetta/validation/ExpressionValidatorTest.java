package com.regnosys.rosetta.validation;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

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
	
	@Test
	void testValidWithMeta() {
		RosettaExpression expr =
				modelService.toTestModel("""
					metaType id string
					metaType scheme string
					""")
				.parseExpression("""
					"someValue" with-meta {
						scheme: "someScheme",
						id: "someId"
					}
					""", 
					"""
					result string (1..1)
				      [metadata scheme]
		              [metadata id]
					""");
			
			validationTestHelper.assertNoIssues(expr);
	}
	
	@Test
	void testInvalidMetaTypeFails() {
		RosettaExpression expr =
				modelService.toTestModel("""
					metaType id string
					metaType scheme string
					""")
				.parseExpression("""
					"someValue" with-meta {
						scheme: 5,
						id: "someId"
					}
					""", 
					"""
                    result string (1..1)
                      [metadata scheme]
                      [metadata id]
                    """);
		
		validationTestHelper.assertError(expr, WITH_META_ENTRY, null, "Expected type `string`, but got `int` instead. Meta attribute 'scheme' should be of type 'string'");
	}

	@Test
	void testWithMetaExpressionIsSingleCardinality() {
		RosettaExpression expr =
				modelService.toTestModel("""
					metaType id string
					metaType scheme string
					""")
						.parseExpression("""
					"someValue" with-meta {
						scheme: ["someScheme", "someOtherScheme"],
						id: "someId"
					}
					""",
					"""
					result string (1..1)
					  [metadata scheme]
					  [metadata id]
					""");

		validationTestHelper.assertError(expr, WITH_META_ENTRY, null, "Expecting single cardinality. Meta attribute 'scheme' was multi cardinality");
	}
}
