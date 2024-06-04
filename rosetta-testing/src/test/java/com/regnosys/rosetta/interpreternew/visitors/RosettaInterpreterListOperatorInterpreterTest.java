package com.regnosys.rosetta.interpreternew.visitors;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterNew;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
class RosettaInterpreterListOperatorInterpreterTest {

	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	@Inject
	private ExpressionValidationHelper validation;
	@SuppressWarnings("unused")
	private ExpressionFactory expFactory;
	
	@BeforeEach
	public void setup() {
		expFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	void testOnlyElementError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) only-element");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testOnlyElementEmptyList() {
		String expected = "List is empty";
		RosettaExpression expr = parser.parseExpression("[] only-element");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		String errorMessage = ((RosettaInterpreterErrorValue)val).getErrors().get(0).getMessage();
		assertEquals(expected, errorMessage);
	}
	
	@Test
	void testOnlyElementMoreThanOne() {
		String expected = "List contains more than one element";
		RosettaExpression expr = parser.parseExpression("[1, 2, 3] only-element");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		String errorMessage = ((RosettaInterpreterErrorValue)val).getErrors().get(0).getMessage();
		assertEquals(expected, errorMessage);
	}
	
	@Test
	void testOnlyElementTrue() {
		RosettaInterpreterNumberValue expected = new RosettaInterpreterNumberValue(BigDecimal.valueOf(1));
		RosettaExpression expr = parser.parseExpression("[1.0] only-element");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterNumberValue);
		RosettaInterpreterNumberValue castedVal = (RosettaInterpreterNumberValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsTrue() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(true);
		RosettaExpression expr = parser.parseExpression("(True and False) exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsFalse() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(false);
		RosettaExpression expr = parser.parseExpression("[] exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsSingleTrue() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(true);
		RosettaExpression expr = parser.parseExpression("[1] single exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsSingleFalse() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(false);
		RosettaExpression expr = parser.parseExpression("[1,2] single exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsMultipleTrue() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(true);
		RosettaExpression expr = parser.parseExpression("[1,2,3] multiple exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsMultipleFalse() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(false);
		RosettaExpression expr = parser.parseExpression("[True] multiple exists");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testExistsError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) single exists");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testIsAbsentTrue() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(true);
		RosettaExpression expr = parser.parseExpression("[] is absent");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testIsAbsentFalse() {
		RosettaInterpreterBooleanValue expected = new RosettaInterpreterBooleanValue(false);
		RosettaExpression expr = parser.parseExpression("[True] is absent");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testIsAbsentError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) is absent");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testCountEmptyList() {
		RosettaInterpreterIntegerValue expected = 
				new RosettaInterpreterIntegerValue(BigInteger.valueOf(0));
		RosettaExpression expr = parser.parseExpression("[] count");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterIntegerValue);
		RosettaInterpreterIntegerValue castedVal = (RosettaInterpreterIntegerValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testCount() {
		RosettaInterpreterIntegerValue expected = 
				new RosettaInterpreterIntegerValue(BigInteger.valueOf(3));
		RosettaExpression expr = parser.parseExpression("[1, 2, 3] count");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterIntegerValue);
		RosettaInterpreterIntegerValue castedVal = (RosettaInterpreterIntegerValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testCountError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) count");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testFirstEmptyList() {
		RosettaExpression expr = parser.parseExpression("[] first");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("List is empty",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testFirst() {
		RosettaInterpreterIntegerValue expected = 
				new RosettaInterpreterIntegerValue(BigInteger.valueOf(3));
		RosettaExpression expr = parser.parseExpression("[3, 4, 5] first");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterIntegerValue);
		RosettaInterpreterIntegerValue castedVal = (RosettaInterpreterIntegerValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testFirstError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) first");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testLastEmptyList() {
		RosettaExpression expr = parser.parseExpression("[] last");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("List is empty",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testLast() {
		RosettaInterpreterIntegerValue expected = 
				new RosettaInterpreterIntegerValue(BigInteger.valueOf(5));
		RosettaExpression expr = parser.parseExpression("[3, 4, 5] last");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterIntegerValue);
		RosettaInterpreterIntegerValue castedVal = (RosettaInterpreterIntegerValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testLastError() {
		RosettaExpression expr = parser.parseExpression("(True and 1) last");

		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean",expr));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}

	@Test
	void testInterpDistinctOperationUseless() {
		String expressionText = "[1 ,2] distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(1), 
						new RosettaInterpreterIntegerValue(2)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpDistinctOperationSimple() {
		String expressionText = "[1 ,2, 2] distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(1), 
						new RosettaInterpreterIntegerValue(2)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpDistinctOperationMany() {
		String expressionText = "[2, 2, 2, 2, 2, 2] distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(2)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpDistinctOperationEmpty() {
		String expressionText = "[] distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of());
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpDistinctOperationNonList() {
		String expressionText = "1 distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(1)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpDistinctOperationError() {
		String expressionText = "[(1 and False), 2] distinct";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		
		assertTrue(val instanceof RosettaInterpreterErrorValue);
	}

	@Test
	void testInterpReverseOperationSimple() {
		String expressionText = "[1 ,2] reverse";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(2), 
						new RosettaInterpreterIntegerValue(1)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpReverseOperationSingle() {
		String expressionText = "1 reverse";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(1)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpReverseOperationEmpty() {
		String expressionText = "[] reverse";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of());
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpReverseOperationBig() {
		String expressionText = "[1 ,2, 3, 4, 5, 6] reverse";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue exp = new RosettaInterpreterListValue(
				List.of(new RosettaInterpreterIntegerValue(6),
						new RosettaInterpreterIntegerValue(5),
						new RosettaInterpreterIntegerValue(4),
						new RosettaInterpreterIntegerValue(3),
						new RosettaInterpreterIntegerValue(2), 
						new RosettaInterpreterIntegerValue(1)));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpReverseOperationError() {
		String expressionText = "[(1 and False), 2] reverse";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		
		assertTrue(val instanceof RosettaInterpreterErrorValue);
	}

	@Test
	void testInterpSumOperationSimple() {
		String expressionText = "[1 ,2] sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterNumberValue exp =
				new RosettaInterpreterNumberValue(BigDecimal.valueOf(3));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpSumOperationSingle() {
		String expressionText = "2 sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterNumberValue exp =
				new RosettaInterpreterNumberValue(BigDecimal.valueOf(2));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpSumOperationMixed() {
		String expressionText = "[2, 3.5, 0.1] sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterNumberValue exp =
				new RosettaInterpreterNumberValue(BigDecimal.valueOf(5.6));
		
		assertEquals(exp, val);
	}
	
	@Test
	void testInterpSumOperationEmpty() {
		String expressionText = "[] sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		
		assertTrue(val instanceof RosettaInterpreterErrorValue);
	}
	
	@Test
	void testInterpSumOperationWrongType() {
		String expressionText = "[1, True, 3] sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		
		assertTrue(val instanceof RosettaInterpreterErrorValue);
	}
	
	@Test
	void testInterpSumOperationErrorInside() {
		String expressionText = "[1, (1 and False), 3] sum";
		RosettaExpression expr = parser.parseExpression(expressionText);
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		
		assertTrue(val instanceof RosettaInterpreterErrorValue);
	}
}
