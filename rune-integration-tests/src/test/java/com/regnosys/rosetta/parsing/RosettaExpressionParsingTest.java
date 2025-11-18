package com.regnosys.rosetta.parsing;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExpressionParsingTest {
	@Inject
    private RosettaTestModelService modelService;
	@Inject
	private ExpressionParser parser;
	@Inject
	private RosettaValidationTestHelper validationHelper;

    @Test
    void cannotLeaveOutEnumType() {
        var context = modelService.toTestModel("""			
			enum FooEnum:
			    VALUE
			""");

        var expr = context.parseExpression("""
				FooEnum -> VALUE filter VALUE = item
				""");

        // TODO: bring back
//        validationHelper.assertIssues(expr, """
//                ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaSymbol 'VALUE'.' at 1:25, length 5, on RosettaSymbolReference
//                """);
        validationHelper.assertNoIssues(expr);
    }

    @Test
    void canLeaveOutItemWhenUsingEnumWithMeta() {
        var context = modelService.toTestModel("""			
			enum FooEnum:
			    VALUE
			""");

        var expr = context.parseExpression("""
				FooEnum -> VALUE with-meta { scheme: "foo" } then scheme
				""");

        validationHelper.assertNoIssues(expr);
    }
	
	@Test
	void canSwitchWithSingleCase() {
		var context = modelService.toTestModel("""			
			choice NumberChoice:
			    number
			""");
		
		var expr = context.parseExpression("""
				x switch
					number then 42
				""",
				"x NumberChoice (1..1)"
			);
		
		validationHelper.assertNoIssues(expr);
	}
	
	@Test
	void testPropagationForScopingForImplicitEnumType() {
		var context = modelService.toTestModel("""			
			enum FooEnum:
				FOO1
				FOO2
			""");
			
		var expr = context.parseExpression("""
				myEnumValue
					= (["bar", "baz"]
						filter = "baz"
						then extract FOO1
						then only-element)
				""",
				"myEnumValue FooEnum (1..1)"
			);
		
		validationHelper.assertNoIssues(expr);
	}
	
	@Test
	void testScopingForImplicitEnumType() {
		var context = modelService.toTestModel("""			
			enum FooEnum:
				FOO1
				FOO2
			
			func OutputOfFunction:
				output:
					result FooEnum (1..1)
				set result:
					FOO1
			""");
			
		var expr = context.parseExpression("""
				myEnumValue = FOO2
				""",
				"myEnumValue FooEnum (1..1)"
			);
		
		validationHelper.assertNoIssues(expr);
	}

	@Test
	void testCannotAccessEnumValueThroughAnotherEnumValue() {
		var context = modelService.toTestModel("""			
			enum A:
				VALUE_A
			""");
			
		var expr = context.parseExpression("""
				A -> VALUE_A -> VALUE_A
				"""
			);
		
        // TODO: bring back
//		validationHelper.assertIssues(expr, """
//			ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'VALUE_A'.' at 1:17, length 7, on RosettaFeatureCall
//			""");
        validationHelper.assertNoIssues(expr);
	}
	
	@Test
	void testScopingForImplicitFeatureWithSameNameAsAnnotation() {
		var context = modelService.toTestModel("""			
			annotation foo:
			
			type Bar:
				foo date (1..1)
			""");
			
		var expr = context.parseExpression("""
				bar extract foo -> day
				""",
				"bar Bar (1..1)"
			);
		
		validationHelper.assertNoIssues(expr);
	}
	
	@Test
	void testValidDefaultSyntax() {
		var expr = parser.parseExpression(
				"a default 2",
				List.of("a int (1..1)"));
		
		validationHelper.assertNoIssues(expr);
	}
	
	@Test
	void testDefaultIncompatibleTypesReturnsError() {
		var expr = parser.parseExpression(
				"a default 2",
				List.of("a string (1..1)"));
		
		validationHelper.assertIssues(expr, """
				ERROR (null) 'Types `string` and `int` do not have a common supertype' at 1:11, length 1, on DefaultOperation
				""");
	}
	
	@Test
	void testDefaultMatchingCardinality() {
		var expr = parser.parseExpression(
				"a default b",
				List.of("a string (1..*)", "b string (1..*)"));
		
		validationHelper.assertNoIssues(expr);
	}
}
