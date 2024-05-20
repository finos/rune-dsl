package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaConditionalExpressionImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterConditionalExpressionTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;

	private ExpressionFactory eFactory;
	
	@BeforeEach
	public void setup() {
		eFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void simpleTestInt() {
		RosettaExpression expr = parser.parseExpression("if True then 1");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(1);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void elseTest1Int() {
		RosettaExpression expr = parser.parseExpression("if False then 1 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(2);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void elseTest2Int() {
		RosettaExpression expr = parser.parseExpression("if True then 1 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(1);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void simpleTestString() {
		RosettaExpression expr = parser.parseExpression("if True then \"abc\"");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals("abc", ((RosettaInterpreterStringValue) result).getValue());
	}
	
	@Test
	public void simpleTestNumber() {
		RosettaExpression expr = parser.parseExpression("if True then 1.2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigDecimal number = BigDecimal.valueOf(1.2);
		
		assertEquals(number, ((RosettaInterpreterNumberValue) result).getValue());
	}
	
	@Test
	public void notSameType() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: consequent and alternative need to have the same type."));
		
		RosettaExpression expr = parser.parseExpression("if True then 1.2 else \"abc\"");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
		
	}
}
