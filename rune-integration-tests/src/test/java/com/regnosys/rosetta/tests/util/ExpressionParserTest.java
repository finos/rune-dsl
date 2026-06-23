package com.regnosys.rosetta.tests.util;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExpressionParserTest {

	@Inject
	private ExpressionParser expressionParser;
	@Inject
	private ExpressionValidationHelper validation;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void simpleExpressionParseTest() {
		RosettaExpression expr = expressionParser.parseExpression("(1 + 1) / 2");
		validation.assertNoIssues(expr);
	}

	@Test
	void expressionWithVariablesParseTest() {
		RosettaExpression expr = expressionParser.parseExpression("(a + b) / 2",
				List.of("a int (1..1)", "b number(max: 5) (0..1)"));
		validation.assertNoIssues(expr);
	}

	@Test
	void expressionWithContextParseTest() {
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
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
				""");

		RosettaExpression expr = expressionParser.parseExpression("foo -> attr - Bar(foo)",
				List.of(model), List.of("foo Foo (1..1)"));
		validation.assertNoIssues(expr);
	}
}
