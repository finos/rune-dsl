package com.regnosys.rosetta.validation;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import javax.inject.Inject;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExpressionValidatorTest {
    @Inject
    private RosettaValidationTestHelper validationTestHelper;
    @Inject
    private RosettaTestModelService modelService;
    
    @Test
    void thenExpressioThatCallsFunctionShouldHaveNoIssues() {
    	RosettaExpression expr = modelService.toTestModel("""
                func SomeFunc:
			 		inputs:
			 			isAlowable boolean (1..1)
			 		output:
			 			result string (1..1)
			 			
			 		set result: if isAlowable then "allowed" else "not allowed"
               """).parseExpression("""
               False then SomeFunc
               """);
    	
    	validationTestHelper.assertNoIssues(expr);
    } 
    
    @Test
    void thenExpressionWithItemShouldHaveNoIssues() {
    	RosettaExpression expr = modelService.toTestModel("""
                type Foo:
                     isAllowable boolean (1..1)
               """).parseExpression("""
               Foo { isAllowable: False } filter isAllowable then extract "someResult"
               """);
    	
    	validationTestHelper.assertNoIssues(expr);
    }    
    
    @Test
    void thenExpressionWithNoItemShouldBeDisallowed() {
    	RosettaExpression expr = modelService.toTestModel("""
                type Foo:
                     isAllowable boolean (1..1)
               """).parseExpression("""
               Foo { isAllowable: False } filter isAllowable then "someResult"
               """);
    	
    	validationTestHelper.assertError(expr, INLINE_FUNCTION, null,
                "The input item is not used in the `then` expression");
    }
    
    @Test
    void enumTypeSymbolReferenceShouldBeDisallowed() {
    	RosettaExpression expr = modelService.toTestModel("""
                enum Foo:
                    VALUE1
                    VALUE2
               """).parseExpression("""
               Foo
               """);
    	
    	validationTestHelper.assertIssues(expr, """
    			ERROR (null) 'Enum type `Foo` must be followed by ` -> <enum value>`. Possible values are: VALUE1, VALUE2' at 1:1, length 3, on RosettaSymbolReference
    			""");
    }

    @Test
    void convertingNonEnumToStringThenUsingToEnumShouldHaveNoIssues() {
        RosettaExpression expr = modelService.toTestModel("""
                enum Bar:
                    VALUE1
                    VALUE2
               """).parseExpression("""
               "VALUE1" to-string to-enum Bar
               """, "foo Foo (1..1)");
        
        validationTestHelper.assertNoIssues(expr);
    }
    
    @Test
    void convertingEnumToAnotherEnumUsingToStringShouldWarn() {
        RosettaExpression expr = modelService.toTestModel("""
                enum Foo:
                    VALUE1
                    VALUE2

                enum Bar:
                    VALUE1
                    VALUE2
               """).parseExpression("""
               foo to-string to-enum Bar
               """, "foo Foo (1..1)");
        
        validationTestHelper.assertIssue(expr, TO_ENUM_OPERATION, null, Severity.WARNING,
                "Using to-string on enumeration to convert to another enum is not required");
    }

    @Test
    void toEnumDoesWorkOnStrings() {
        RosettaExpression expr = modelService.toTestModel("""
                 enum Bar:
                     VALUE1
                     VALUE2
                """).parseExpression("""
                "VALUE1" to-enum Bar
                """);

        validationTestHelper.assertNoIssues(expr);
    }

    @Test
    void toEnumDoesWorkOnEnum() {
        RosettaExpression expr = modelService.toTestModel("""
                 enum Foo:
                     VALUE1
                     VALUE2

                 enum Bar:
                     VALUE1
                     VALUE2
                """).parseExpression("""
                foo to-enum Bar
                """, "foo Foo (1..1)");

        validationTestHelper.assertNoIssues(expr);
    }

    @Test
    void toEnumDoesNotWorkOnInt() {
        RosettaExpression expr = modelService.toTestModel("""
                 enum Bar:
                     VALUE1
                     VALUE2
                """).parseExpression("""
                123 to-enum Bar
                """);

        validationTestHelper.assertError(expr, TO_ENUM_OPERATION, null,
                "Operator `to-enum` is not supported for type `int`. Supported argument types are strings and enumerations");
    }

    @Test
    void testValidChoiceConstruction() {
        RosettaExpression expr = modelService.toTestModel("""
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
        RosettaExpression expr = modelService.toTestModel("""
                metaType id string
                metaType scheme string
                """).parseExpression("""
                "someValue" with-meta {
                	scheme: "someScheme",
                	id: "someId"
                }
                """, """
                result string (1..1)
                     [metadata scheme]
                           [metadata id]
                """);

        validationTestHelper.assertNoIssues(expr);
    }

    @Test
    void testInvalidMetaTypeFails() {
        RosettaExpression expr = modelService.toTestModel("""
                metaType id string
                metaType scheme string
                """).parseExpression("""
                "someValue" with-meta {
                	scheme: 5,
                	id: "someId"
                }
                """, """
                result string (1..1)
                  [metadata scheme]
                  [metadata id]
                """);

        validationTestHelper.assertError(expr, WITH_META_ENTRY, null,
                "Expected type `string`, but got `int` instead. Meta attribute 'scheme' should be of type 'string'");
    }

    @Test
    void testWithMetaExpressionIsSingleCardinality() {
        RosettaExpression expr = modelService.toTestModel("""
                metaType id string
                metaType scheme string
                """).parseExpression("""
                "someValue" with-meta {
                	scheme: ["someScheme", "someOtherScheme"],
                	id: "someId"
                }
                """, """
                result string (1..1)
                  [metadata scheme]
                  [metadata id]
                """);

        validationTestHelper.assertError(expr, WITH_META_ENTRY, null,
                "Expecting single cardinality. Meta attribute 'scheme' was multi cardinality");
    }

    @Test
    void testWithMetaArgumentIsSingleCardinality() {
        RosettaExpression expr = modelService.toTestModel("""
                metaType id string
                metaType scheme string
                """).parseExpression("""
                ["someValue", "someOtherValue"] with-meta {
                	scheme: "someScheme",
                	id: "someId"
                }
                """, """
                result string (1..1)
                  [metadata scheme]
                  [metadata id]
                """);

        validationTestHelper.assertError(expr, WITH_META_OPERATION, null,
                "Expecting single cardinality. The with-meta operator can only be used with single cardinality arguments");
    }
}
