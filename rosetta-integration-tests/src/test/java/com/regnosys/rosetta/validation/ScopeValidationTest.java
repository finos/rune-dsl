package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ScopeValidationTest {
	@Inject
	private RosettaValidationTestHelper validationTestHelper;
	@Inject
	private ExpressionParser expressionParser;
	
	@Test
	void testUnresolvedReferenceInConstructorTypeShouldNotPropagate() {
		RosettaExpression expr =
			expressionParser.parseExpression("""
				Missing {
					attr: 42
				}
				""");
		
		validationTestHelper.assertIssues(expr, """
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaType 'Missing'.' on TypeCall, offset 0, length 3
				""");
	}
	
	@Test
	void testUnresolvedReferenceInPathShouldNotPropagate() {
		RosettaExpression expr =
			expressionParser.parseExpression("""
				a -> b
				""");
		
		validationTestHelper.assertIssues(expr, """
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaSymbol 'a'.' on RosettaSymbolReference, offset 0, length 1
				""");
	}
	
	@Test
	void testUnresolvedReferenceInSwitchShouldNotPropagate() {
		RosettaExpression expr =
			expressionParser.parseExpression("""
				foo switch
				  VALUE then 42,
				  default 0
				""");
		
		validationTestHelper.assertIssues(expr, """
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaSymbol 'foo'.' on RosettaSymbolReference, offset 0, length 3
				""");
	}
}
