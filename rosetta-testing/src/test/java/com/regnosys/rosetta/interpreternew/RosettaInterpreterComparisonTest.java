package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
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
	
	private ExpressionFactory eFactory;
	
	@BeforeEach
	public void setup() {
		eFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void intSmallerTest() {
		RosettaExpression expr = parser.parseExpression("1 < 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
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
	public void cardinalityAllListsTest() {
		RosettaExpression expr = parser.parseExpression("[1,2,3] all > [0]");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
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
	
}
