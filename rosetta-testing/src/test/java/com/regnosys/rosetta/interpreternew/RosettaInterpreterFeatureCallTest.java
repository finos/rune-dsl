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

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterFeatureCallTest {

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
	
	RosettaInterpreterErrorValue error1 = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
			"Feature calls: the receiver is an error value."));
	RosettaInterpreterErrorValue error2 = new RosettaInterpreterErrorValue(new RosettaInterpreterError(
			"dates does not exist in the environment"));
	
	@Test
	public void testDateDay() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2024 } -> day");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(day, result);
	}
	
	@Test
	public void testDateMonth() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2024 } -> month");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(month, result);
	}
	
	@Test
	public void testDateYear() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2024 } -> year");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(year, result);
	}
	
	@Test
	public void testDateTimeTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 5, month: 7, year: 2024 }, time: t } -> time", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(time, result);
	}
	
	@Test
	public void testDateTimeDay() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 5, month: 7, year: 2024 }, time: t } -> date -> day", 
				List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(day, result);
	}
	
	@Test
	public void testZonedDateTimeTimeZone() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" } -> timezone", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals("CET", ((RosettaInterpreterStringValue) result).getValue());
	}
	
	@Test
	public void testZonedDateTimeDate() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" } -> date", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(date, result);
	}
	
	@Test
	public void testZonedDateTimeDateYear() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" } -> date -> year", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(year, result);
	}
	
	@Test
	public void testZonedDateTimeTime() {
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		env.addValue("t", time);
		
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2024 }"
				+ ", time: t, timezone: \"CET\" } -> time", List.of("t time (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr, env);
		
		assertEquals(time, result);
	}
	
	@Test
	public void testError() {
		RosettaExpression expr = parser.parseExpression(
				"dates -> day", List.of("dates date (1..1)"));
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		RosettaInterpreterErrorValue errors = RosettaInterpreterErrorValue.merge(error1, error2);
		assertEquals(errors, result);
	}
}
