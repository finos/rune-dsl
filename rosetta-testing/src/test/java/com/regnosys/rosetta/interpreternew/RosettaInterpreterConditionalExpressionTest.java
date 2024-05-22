package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
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
	public void integerTest() {
		RosettaExpression expr = parser.parseExpression("if True then 1");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(1);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void integerElseTest() {
		RosettaExpression expr = parser.parseExpression("if False then 1 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(2);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void integerThenTest() {
		RosettaExpression expr = parser.parseExpression("if True then 1 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(1);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void booleanTest() {
		RosettaExpression expr = parser.parseExpression("if True then False");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(false, ((RosettaInterpreterBooleanValue) result).getValue());
	}
	
	@Test
	public void stringTest() {
		RosettaExpression expr = parser.parseExpression("if True then \"abc\"");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals("abc", ((RosettaInterpreterStringValue) result).getValue());
	}
	
	@Test
	public void numberTest() {
		RosettaExpression expr = parser.parseExpression("if True then 1.2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigDecimal number = BigDecimal.valueOf(1.2);
		
		assertEquals(number, ((RosettaInterpreterNumberValue) result).getValue());
	}
	
	@Test
	public void listTest() {
		RosettaExpression expr = parser.parseExpression("if True then [1, 2]");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		RosettaInterpreterIntegerValue one = new RosettaInterpreterIntegerValue(BigInteger.valueOf(1));
		RosettaInterpreterIntegerValue two = new RosettaInterpreterIntegerValue(BigInteger.valueOf(2));
		
		List<RosettaInterpreterValue> list = List.of(one, two);
		
		assertEquals(list, ((RosettaInterpreterListValue) result).getExpressions());
	}
	
	@Test
	public void complexTest() {
		RosettaExpression expr = parser.parseExpression("if 3 > 2 then 1 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		BigInteger number = BigInteger.valueOf(1);
		
		assertEquals(number, ((RosettaInterpreterIntegerValue) result).getValue());
	}
	
	@Test
	public void errorThenTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: then is an error value."));
		expected.addError(new RosettaInterpreterError("cannot use \"ALL\" keyword " + "to compare two elements"));
		
		RosettaExpression expr = parser.parseExpression("if True then 1 all = 3 else 2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void errorElseTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: else is an error value."));
		expected.addError(new RosettaInterpreterError("cannot use \"ALL\" keyword " + "to compare two elements"));
		
		RosettaExpression expr = parser.parseExpression("if False then 2 else 1 all = 3");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void notSameTypeThenTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: then and else need to have the same type."));
		
		RosettaExpression expr = parser.parseExpression("if True then 1.2 else \"abc\"");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void notSameTypeElseTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: then and else need to have the same type."));
		
		RosettaExpression expr = parser.parseExpression("if False then 1.2 else \"abc\"");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void conditionNotBooleanTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: condition is not a boolean value."));
		
		RosettaExpression expr = parser.parseExpression("if 1 then 1.2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void conditionErrorTypeTest() {
		RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: condition is an error value."));
		expected.addError(new RosettaInterpreterError("cannot use \"ALL\" keyword " + "to compare two elements"));
		
		RosettaExpression expr = parser.parseExpression("if 1 all = 3 then 1.2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		RosettaInterpreterErrorValue errorResult = (RosettaInterpreterErrorValue) result;
		
		assertEquals(expected.getErrors(), errorResult.getErrors());
		assertEquals(expected.getErrors().get(0).getMessage(), errorResult.getErrors().get(0).getMessage());
	}
	
	@Test
	public void noElseTest() {
		RosettaExpression expr = parser.parseExpression("if False then 1.2");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(null, result);
	}

}
