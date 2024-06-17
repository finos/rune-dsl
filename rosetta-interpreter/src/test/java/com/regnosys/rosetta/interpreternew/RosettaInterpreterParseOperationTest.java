package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ToNumberOperationImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterParseOperationTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	RosettaInterpreterListValue empty = new RosettaInterpreterListValue(List.of());
	
	@Test
	void toNumberStringTest() {
		RosettaExpression expr = parser.parseExpression("\"3.4\" to-number");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(new RosettaInterpreterNumberValue(3.4), result);
	}
	
	@Test
	void toNumberError() {
		RosettaExpression expr = parser.parseExpression("\"bla\" to-number");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toIntStringTest() {
		RosettaExpression expr = parser.parseExpression("\"3\" to-int");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(new RosettaInterpreterNumberValue(3), result);
	}
	
	@Test
	void toIntError() {
		RosettaExpression expr = parser.parseExpression("\"3.4\" to-int");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
}
