package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.*;

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
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterLiteralsTest {
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
	public void BooleanTest() {
		RosettaExpression expr = parser.parseExpression("True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(true, ((RosettaInterpreterBooleanValue)val).getValue());
	}
	 
	@Test
	public void ListTest() {
		RosettaExpression expr = parser.parseExpression("[1,2]");
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue expected = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterIntegerValue(BigInteger.valueOf(1)), 
						new RosettaInterpreterIntegerValue(BigInteger.valueOf(2))));
		assertTrue(expected.equals(val));
		
	}
	
	@Test
	public void IntTest() {
		RosettaExpression expr = parser.parseExpression("5");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(BigInteger.valueOf(5), ((RosettaInterpreterIntegerValue)val).getValue());
	}
	
	@Test
	public void NumberTest() {
		RosettaExpression expr = parser.parseExpression("5.5");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(BigDecimal.valueOf(5.5), ((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void StringTest() {
		RosettaExpression expr = parser.parseExpression("\"hello\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("hello", ((RosettaInterpreterStringValue)val).getValue());
	}
}
