package com.regnosys.rosetta.interpreternew;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaIntLiteralImpl;

import static org.junit.jupiter.api.Assertions.*;
import javax.inject.Inject;

import java.math.BigInteger;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterNewTest {
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	private ExpressionFactory eFactory;
	
	@BeforeEach
	public void setup() {
		eFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void TestTest() {
		assertEquals(5, interpreter.Test());
	}
	
	@Test
	public void ExampleInterpTest() {
		final int testValue = 10;
		
		RosettaIntLiteral expr = eFactory.createRosettaIntLiteral();
		expr.setValue(BigInteger.valueOf(testValue));
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterIntegerValue);
		RosettaInterpreterIntegerValue intVal = (RosettaInterpreterIntegerValue)val;
		assertEquals(testValue, intVal.getValue());
	}
	
	@Test
	public void VisitorInitialTest() {
		final boolean testValue = false;
		
		RosettaBooleanLiteral expr = eFactory.createRosettaBooleanLiteral();
		expr.setValue(testValue);
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue boolVal = (RosettaInterpreterBooleanValue)val;
		assertEquals(testValue, boolVal.getValue());
	}
}
