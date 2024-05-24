package com.regnosys.rosetta.interpreternew.visitors;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

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
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
class RosettaInterpreterListOperatorInterpreterTest {
	
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	
	@SuppressWarnings("unused")
	private ExpressionFactory expFactory;
	
	@BeforeEach
	public void setup() {
		expFactory = ExpressionFactoryImpl.init();
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
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean"));
		RosettaExpression expr = parser.parseExpression("(True and 1) single exists");
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
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean"));
		RosettaExpression expr = parser.parseExpression("(True and 1) is absent");
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
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean"));
		RosettaExpression expr = parser.parseExpression("(True and 1) count");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testFirstEmptyList() {
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("List is empty"));
		RosettaExpression expr = parser.parseExpression("[] first");
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
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean"));
		RosettaExpression expr = parser.parseExpression("(True and 1) first");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
	@Test
	void testLastEmptyList() {
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("List is empty"));
		RosettaExpression expr = parser.parseExpression("[] last");
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
		RosettaInterpreterErrorValue expected = 
				new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Logical Operation: "
						+ "Rightside is not of type Boolean"));
		RosettaExpression expr = parser.parseExpression("(True and 1) last");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		RosettaInterpreterErrorValue castedVal = (RosettaInterpreterErrorValue)val;
		assertEquals(expected, castedVal);
	}
	
}
