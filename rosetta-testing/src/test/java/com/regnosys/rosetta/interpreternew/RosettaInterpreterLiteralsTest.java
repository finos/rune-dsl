package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.rosetta.model.lib.RosettaNumber;
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterLiteralsTest {
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	@Inject
	private ExpressionValidationHelper validation;
	
	@SuppressWarnings("unused")
	private ExpressionFactory exFactory;
	
	@BeforeEach
	public void setup() {
		exFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void booleanTest() {
		RosettaExpression expr = parser.parseExpression("True");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(true, ((RosettaInterpreterBooleanValue)val).getValue());
	}
	 
	@Test
	public void listTest() {
		RosettaExpression expr = parser.parseExpression("[1,2]");
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue expected = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(2))));
		assertEquals(expected, val);
		
	}
	
	@Test
	public void nestedListTest() {
		RosettaExpression expr = parser.parseExpression("[1,[2,3]]");
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue expected = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(2)),
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(3))));
		assertEquals(expected, val);
		
	}
	
	@Test
	public void veryNestedListTest() {
		RosettaExpression expr = parser.parseExpression("[1,[2,[3, [4, [5]]]]]");
		validation.assertNoIssues(expr);
		RosettaInterpreterValue val = interpreter.interp(expr);
		RosettaInterpreterListValue expected = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(2)),
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(3)),
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(4)),
						new RosettaInterpreterNumberValue(
								BigDecimal.valueOf(5))));
		assertEquals(expected, val);
		
	}
	
	@Test
    public void emptyElementInListTest() {
        RosettaExpression expr = parser.parseExpression("[1, [2, 3], empty]");
        validation.assertNoIssues(expr);
        RosettaInterpreterValue val = interpreter.interp(expr);
        RosettaInterpreterListValue expected = 
                new RosettaInterpreterListValue(List.of(
                        new RosettaInterpreterNumberValue(
                                BigDecimal.valueOf(1)), 
                        new RosettaInterpreterNumberValue(
                                BigDecimal.valueOf(2)),
                        new RosettaInterpreterNumberValue(
                                BigDecimal.valueOf(3))));
        assertEquals(expected, val);
    }
	
	@Test
	public void intTest() {
		RosettaExpression expr = parser.parseExpression("5");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(5)),
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void numberTest() {
		RosettaExpression expr = parser.parseExpression("5.5");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(RosettaNumber.valueOf(BigDecimal.valueOf(5.5)), 
				((RosettaInterpreterNumberValue)val).getValue());
	}
	
	@Test
	public void intEqualsFloatTest() {
		RosettaExpression expr = parser.parseExpression("5 = 5.0");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(true, 
				((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void intEqualsDoubleTest() {
		RosettaExpression expr = parser.parseExpression("5 = 5.00");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(true, 
				((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void floatEqualsDoubleTest() {
		RosettaExpression expr = parser.parseExpression("5.0 = 5.00");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals(true, 
				((RosettaInterpreterBooleanValue)val).getValue());
	}
	
	@Test
	public void stringTest() {
		RosettaExpression expr = parser.parseExpression("\"hello\"");
		RosettaInterpreterValue val = interpreter.interp(expr);
		assertEquals("hello", ((RosettaInterpreterStringValue)val).getValue());
	}
}
