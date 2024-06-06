package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterComparisonTest {
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	
	@SuppressWarnings("unused")
	private ExpressionFactory exFactory;
	
	private ComparisonOperation createComparisonOperation(
    		String operator, RosettaExpression left, RosettaExpression right, CardinalityModifier cardMod) {
		ComparisonOperation operation = exFactory.createComparisonOperation();
        operation.setOperator(operator);
        operation.setLeft(left);
        operation.setRight(right);
        operation.setCardMod(cardMod);
        return operation;
    }
	
	
	@BeforeEach
	public void setup() {
		exFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void nestedTest() {
		RosettaExpression expr = parser.parseExpression("True = (1 < 3)");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void intSmallerTrueTest() {
		RosettaExpression expr = parser.parseExpression("1 < 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void intSmallerFalseTest() {
		RosettaExpression expr = parser.parseExpression("2 < 1");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void stringBiggerTest() {
		RosettaExpression expr = parser.parseExpression("\"bro\" > \"dude\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	
	@Test
	public void booleanLessEqualTest() {
		RosettaExpression expr = parser.parseExpression("False <= False");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void numberLessEqualTest() {
		RosettaExpression expr = parser.parseExpression("2 <= 1");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void numberMoreEqualTest() {
		RosettaExpression expr = parser.parseExpression("1 >= 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void cardinalityAllListsTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"cannot compare two lists"));
		RosettaExpression expr = parser.parseExpression("[1,2,3] all >= [0]");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(expectedError.getErrors().get(0).getMessage(),
				((RosettaInterpreterErrorValue)val)
					.getErrors().get(0).getMessage());
	}
	
	@Test
	public void cardinalityAllSimpleTest() {
		RosettaExpression expr = parser.parseExpression("[1,2,3] all < 4");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void cardinalityAnySimpleTest() {
		RosettaExpression expr = parser.parseExpression("[1,2,3] any > 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void errorThrownListTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"cannot compare two lists"));
		RosettaExpression expr = parser.parseExpression("[1,2,3] any <= [1,2]");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(expectedError.getErrors(),
				((RosettaInterpreterErrorValue)val).getErrors());
		assertEquals(expectedError.getErrors().get(0).getMessage(),
				((RosettaInterpreterErrorValue)val)
					.getErrors().get(0).getMessage());
	}
	
	@Test
	public void errorThrownAllElementsTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"cannot use \"ALL\" keyword "
								+ "to compare two elements"));
		RosettaExpression expr = parser.parseExpression("1 all > 3");
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorVal = (RosettaInterpreterErrorValue) val;
		assertEquals(expectedError.getErrors(),
				(errorVal.getErrors()));
		
		assertEquals(((RosettaInterpreterError)expectedError.getErrors().get(0)).getError(),
				(((RosettaInterpreterError)(errorVal.getErrors().get(0)))
						.getError()));
	}
	
	@Test
	void badOperatorTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"operator not suppported")); 
		
		RosettaBooleanLiteral ex = exFactory.createRosettaBooleanLiteral();
        ex.setValue(true);
		RosettaExpression expr = createComparisonOperation("><", ex,ex, CardinalityModifier.NONE);
		
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorVal = (RosettaInterpreterErrorValue) val;
		
		assertEquals(((RosettaInterpreterError)expectedError.getErrors().get(0)).getError(),
				(((RosettaInterpreterError)(errorVal.getErrors().get(0)))
						.getError()));
		
	}
	
	@Test
	void wrongListLengthAnyTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"cannot use \"ANY\" keyword "
								+ "to compare two elements"));
		RosettaExpression expr = parser.parseExpression("[1] any < 3");
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorVal = (RosettaInterpreterErrorValue) val;
		assertEquals(expectedError.getErrors(),
				(errorVal.getErrors()));
		
		assertEquals(((RosettaInterpreterError)expectedError.getErrors().get(0)).getError(),
				(((RosettaInterpreterError)(errorVal.getErrors().get(0)))
						.getError()));
	}
	
	@Test
	void wrongListLengthAllTest() {
		RosettaInterpreterErrorValue expectedError = new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
						"cannot use \"ALL\" keyword "
								+ "to compare two elements"));
		RosettaExpression expr = parser.parseExpression("[1] all <= 3");
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorVal = (RosettaInterpreterErrorValue) val;
		assertEquals(expectedError.getErrors(),
				(errorVal.getErrors()));
		
		assertEquals(((RosettaInterpreterError)expectedError.getErrors().get(0)).getError(),
				(((RosettaInterpreterError)(errorVal.getErrors().get(0)))
						.getError()));
	}
	
}
