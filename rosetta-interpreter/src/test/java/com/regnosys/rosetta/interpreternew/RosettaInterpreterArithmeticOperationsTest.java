package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.rosetta.model.lib.RosettaNumber;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterArithmeticOperationsTest {
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;

	@SuppressWarnings("unused")
	private ExpressionFactory exFactory;
	
	@BeforeEach
	public void setup() {
		exFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void plusTest() {
		RosettaExpression expr = parser.parseExpression("1+2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(3)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void compositePlusTest() {
		RosettaExpression expr = parser.parseExpression("1+2+3");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(6)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void decimalPlusTest() {
		RosettaExpression expr = parser.parseExpression("1.2 + 2.7");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(3.9)),
				((RosettaInterpreterNumberValue)val).getValue());
	}

	@Test
	public void minusTest() {
		RosettaExpression expr = parser.parseExpression("1-2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(-1)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void multiplyTest() {
		RosettaExpression expr = parser.parseExpression("5*2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(10)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void divideTest() {
		RosettaExpression expr = parser.parseExpression("6/2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(3)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void divisionByZeroTest() {
		RosettaExpression expr = parser.parseExpression("6/0");
		RosettaInterpreterValue val = interpreter.interp(expr);
		List<RosettaInterpreterError> expected = List.of(
				new RosettaInterpreterError(
						"Division by 0 is not allowed"));
		
		assertEquals(expected,
				((RosettaInterpreterErrorValue)val).getErrors());
	}

	@Test
	public void stringConcatenationTest() {
		RosettaExpression expr = parser.parseExpression("\"Hello \" + \"World\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("Hello World", ((RosettaInterpreterStringValue)val).getValue());
	}
	
	@Test
	public void correctTypesMixedTest() {
		RosettaExpression expr = parser.parseExpression("\"Hello \" + 5");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("The terms of the operation "
				+ "are neither both strings nor both numbers", 
				((RosettaInterpreterErrorValue)val).getErrors()
				.get(0).getMessage());
	}
	
	@Test
	public void correctTypesMixedTestTheOtherWay() {
		RosettaExpression expr = parser.parseExpression("5 + \"Hello\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("The terms of the operation "
				+ "are neither both strings nor both numbers", 
				((RosettaInterpreterErrorValue)val).getErrors()
				.get(0).getMessage());
	}
	
	@Test
	public void stringConcatenationErrorTest() {
		RosettaExpression expr = parser.parseExpression("\"Hello \" - \"World\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("The terms are strings but the operation "
				+ "is not concatenation: not implemented", 
				((RosettaInterpreterErrorValue)val).getErrors()
				.get(0).getMessage());
	}
	
	@Test
	public void wrongTypeLeftTest() {
		RosettaExpression expr = parser.parseExpression("True - \"World\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("Arithmetic Operation: Leftside is not of type Number/String", 
				((RosettaInterpreterErrorValue)val)
				.getErrors().get(0).getMessage());
	}
	
	@Test
	public void wrongTypeRightTestString() {
		RosettaExpression expr = parser.parseExpression("\"Hello \" + True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("Arithmetic Operation: Rightside is not of type Number/String", 
				((RosettaInterpreterErrorValue)val)
				.getErrors().get(0).getMessage());
	}
	
	@Test
	public void wrongTypeRightTestInteger() {
		RosettaExpression expr = parser.parseExpression("2 + True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("Arithmetic Operation: Rightside is not of type Number/String", 
				((RosettaInterpreterErrorValue)val)
				.getErrors().get(0).getMessage());
	}
	
	@Test
	public void wrongTypeRightTestNumber() {
		RosettaExpression expr = parser.parseExpression("2.5 + True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("Arithmetic Operation: Rightside is not of type Number/String", 
				((RosettaInterpreterErrorValue)val)
				.getErrors().get(0).getMessage());
	}
	
	@Test
	public void leftsideErrorTest() {
		RosettaExpression expr = parser
				.parseExpression("\"Hello \" - \"World\" + \"World\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("The terms are strings but the "
				+ "operation is not concatenation: not implemented", 
				((RosettaInterpreterErrorValue)val)
				.getErrors().get(0).getMessage());
	}
	
	@Test
	public void complexTest() {
		RosettaExpression expr = parser
				.parseExpression("(\"Hello \" - \"World\") + (2 + ([1, 2] any > 0))");
		RosettaInterpreterValue val = interpreter.interp(expr);
		List<RosettaInterpreterError> expected = List.of(
				new RosettaInterpreterError(
						"The terms are strings but the operation "
						+ "is not concatenation: not implemented"),
				new RosettaInterpreterError(
						"Arithmetic Operation: Rightside "
						 + "is not of type Number/String")
					);
		assertEquals(expected, 
				((RosettaInterpreterErrorValue)val)
				.getErrors());
	}
}
