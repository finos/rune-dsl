package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaConstructorExpressionImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterConstructorExpressionTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Inject
	ModelHelper mh;
	
	RosettaInterpreterIntegerValue day = new RosettaInterpreterIntegerValue(BigInteger.valueOf(5));
	RosettaInterpreterIntegerValue month = new RosettaInterpreterIntegerValue(BigInteger.valueOf(7));
	RosettaInterpreterIntegerValue year = new RosettaInterpreterIntegerValue(BigInteger.valueOf(2024));
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(day, month, year);
	
	RosettaInterpreterNumberValue hours = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
	RosettaInterpreterNumberValue minutes = new RosettaInterpreterNumberValue(BigDecimal.valueOf(30));
	RosettaInterpreterNumberValue seconds = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	
	RosettaInterpreterNumberValue hoursError = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue timeError = new RosettaInterpreterTimeValue(hoursError, minutes, seconds);
	
	RosettaInterpreterErrorValue error = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
			"Constructor Expressions: time isn't valid."));
	RosettaInterpreterErrorValue errorAtt = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
			"Constructor Expressions: attribute type is not valid."));
	
	@Test
	public void testDate() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2024 }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(day, ((RosettaInterpreterDateValue) result).getDay());
		assertEquals(month, ((RosettaInterpreterDateValue) result).getMonth());
		assertEquals(year, ((RosettaInterpreterDateValue) result).getYear());
	}
	
	@Test
	public void testDateNotValidDay() {
		RosettaExpression expr = parser.parseExpression("date { day: \"F\", month: 7, year: 2024 }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testDateNotValidMonth() {
		RosettaExpression expr = parser.parseExpression("date { day: 3, month: True, year: 2024 }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testDateNotValidYear() {
		RosettaExpression expr = parser.parseExpression("date { day: 3, month: 7, year: [1, 2] }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testDateTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 5, month: 7, year: 2024 }, time: t }", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(day, ((RosettaInterpreterDateTimeValue) result).getDate().getDay());
		assertEquals(month, ((RosettaInterpreterDateTimeValue) result).getDate().getMonth());
		assertEquals(year, ((RosettaInterpreterDateTimeValue) result).getDate().getYear());
		assertEquals(time, ((RosettaInterpreterDateTimeValue) result).getTime());
	}
	
	@Test
	public void testDateTimeNotValid() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", timeError);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 5, month: 7, year: 2024 }, time: t }", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(error, result);
	}
	
	@Test
	public void testDateTimeNotValidDate() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: \"F\", month: 7, year: 2024 }, time: t }", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testDateTimeNotValidTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", day);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 3, month: 7, year: 2024 }, time: t }", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testZonedDateTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" }", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(day, ((RosettaInterpreterZonedDateTimeValue) result).getDate().getDay());
		assertEquals(month, ((RosettaInterpreterZonedDateTimeValue) result).getDate().getMonth());
		assertEquals(year, ((RosettaInterpreterZonedDateTimeValue) result).getDate().getYear());
		assertEquals(time, ((RosettaInterpreterZonedDateTimeValue) result).getTime());
		assertEquals("CET", ((RosettaInterpreterZonedDateTimeValue) result).getTimeZone().getValue());
	}
	
	@Test
	public void testZonedDateTimeNotValid() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", timeError);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" }", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(error, result);
	}
	
	@Test
	public void testZonedDateTimeNotValidDate() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: True, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" }", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testZonedDateTimeNotValidTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", month);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" }", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testZonedDateTimeNotValidZone() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: 56 }", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(errorAtt, result);
	}
	
	@Test
	public void testDataType() {
		RosettaModel model = mh.parseRosettaWithNoErrors("type Person: name string (1..1) "
				+ "func M: output: result Person (1..1) set result: Person { name: \"F\" }");
		
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) model.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue result = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		
		assertEquals("Person", result.getName());
		assertEquals("name", result.getAttributes().get(0).getName());
		assertEquals("F", ((RosettaInterpreterStringValue) result.getAttributes().get(0).getValue())
				.getValue());
	}
	
	@Test
	public void testDataTypeError() {
		RosettaModel model = mh.parseRosetta("type Test: value boolean (1..1) "
				+ "func M: output: result Test (1..1) set result: Test { value: 1 and True }");
		
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) model.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterValue result = interpreter.interp(constructor);
		
		RosettaInterpreterErrorValue errorBool = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
				"Logical Operation: Leftside is not of type Boolean"));
		RosettaInterpreterErrorValue errorValue = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
				"Constructor Expression: the attribute \"value\" is an error value."));
		
		assertEquals(RosettaInterpreterErrorValue.merge(errorValue, errorBool), result);
	}
}
