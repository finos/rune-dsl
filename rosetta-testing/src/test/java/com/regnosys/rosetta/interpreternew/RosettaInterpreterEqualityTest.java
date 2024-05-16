package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.*;

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
public class RosettaInterpreterEqualityTest {
	
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
	public void equalityTrueTest() {
		RosettaExpression expr = parser.parseExpression("1 = 1");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityFalseTest() {
		RosettaExpression expr = parser.parseExpression("1 = 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void inequalityTrueTest() {
		RosettaExpression expr = parser.parseExpression("1 <> 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityDifferentTypesTest() {
		RosettaExpression expr = parser.parseExpression("1 = True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityAllTrueTest() {
		RosettaExpression expr = parser.parseExpression("[1,1,1] all = 1");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityAllFalseTest() {
		RosettaExpression expr = parser.parseExpression("[1,2,1] all = 1");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityAnyFalseTest() {
		RosettaExpression expr = parser.parseExpression("[2,2,2] any <> 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertFalse(((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void equalityAnyTrueTest() {
		RosettaExpression expr = parser.parseExpression("[1,2,3] any = 2");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(((RosettaInterpreterBooleanValue)val).getValue());
	}

}
