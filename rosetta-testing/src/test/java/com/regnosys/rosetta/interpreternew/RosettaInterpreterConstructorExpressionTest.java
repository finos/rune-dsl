package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
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
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterConstructorExpressionTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	RosettaInterpreterNumberValue day = new RosettaInterpreterNumberValue(5);
	RosettaInterpreterNumberValue month = new RosettaInterpreterNumberValue(7);
	RosettaInterpreterNumberValue year = new RosettaInterpreterNumberValue(2024);
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(day, month, year);
	
	RosettaInterpreterNumberValue hours = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
	RosettaInterpreterNumberValue minutes = new RosettaInterpreterNumberValue(BigDecimal.valueOf(30));
	RosettaInterpreterNumberValue seconds = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	
	RosettaInterpreterNumberValue hoursError = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue timeError = new RosettaInterpreterTimeValue(hoursError, minutes, seconds);
	
	RosettaInterpreterErrorValue error = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
			"Constructor Expressions: time isn't valid."));
	
	@Test
	public void testDate() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2024 }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(day, ((RosettaInterpreterDateValue) result).getDay());
		assertEquals(month, ((RosettaInterpreterDateValue) result).getMonth());
		assertEquals(year, ((RosettaInterpreterDateValue) result).getYear());
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
}
