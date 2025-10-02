package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import javax.inject.Inject
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ExpressionParserTest {

	@Inject extension ExpressionParser
	@Inject extension ExpressionValidationHelper
	@Inject extension ModelHelper
	
	@Test
	def void simpleExpressionParseTest() {
		"(1 + 1) / 2"
			.parseExpression
			.assertNoIssues
	}
	
	@Test
	def void expressionWithVariablesParseTest() {
		"(a + b) / 2"
			.parseExpression(#["a int (1..1)", "b number(max: 5) (0..1)"])
			.assertNoIssues
	}
	
	@Test
	def void expressionWithContextParseTest() {
		val model = '''
		type Foo:
			attr int (0..1)
		
		func Bar:
			inputs:
				foo Foo (1..1)
			output:
				result int (1..1)
			
			alias part: foo -> attr + 1
			
			set result:
				part + 1
		'''.parseRosettaWithNoIssues
		
		"foo -> attr - Bar(foo)"
			.parseExpression(#[model], #["foo Foo (1..1)"])
			.assertNoIssues
	}
}
