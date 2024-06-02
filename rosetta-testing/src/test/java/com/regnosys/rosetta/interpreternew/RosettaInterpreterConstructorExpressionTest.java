package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
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
	
	@Test
	public void test() {
		RosettaModel expr = mh.parseRosettaWithNoErrors("recordType date\r\n"
				+ "{\r\n"
				+ "	day   int\r\n"
				+ "	month int\r\n"
				+ "	year  int }");
		System.out.println(expr.getElements().get(0).getModel());
	}
	
	@Test
	public void testDate() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2000 }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(5, ((RosettaInterpreterDateValue) result).getDay());
		assertEquals(7, ((RosettaInterpreterDateValue) result).getMonth());
		assertEquals(2000, ((RosettaInterpreterDateValue) result).getYear());
	}
	
	@Test
	public void testDateTime() {
		RosettaExpression expr = parser.parseExpression(
				"dateTime { date: date { day: 5, month: 7, year: 2022 }, time: \"05:00:00\" }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(5, 7, 2022);
		RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(LocalTime.now());
		
		assertEquals(date, ((RosettaInterpreterDateTimeValue) result).getDate());
		assertEquals(time, ((RosettaInterpreterDateTimeValue) result).getTime());
	}
	
	@Test
	public void testZonedDateTime() {
		RosettaExpression expr = parser.parseExpression(
				"zonedDateTime { date: date { day: 5, month: 7, year: 2022 }"
				+ ", time: \"05:00:00\", timezone: \"CET\" }");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(5, 7, 2022);
		RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(LocalTime.now());
		
		assertEquals(date, ((RosettaInterpreterDateTimeValue) result).getDate());
		assertEquals(time, ((RosettaInterpreterDateTimeValue) result).getTime());
		assertEquals("CET", ((RosettaInterpreterStringValue) result).getValue());
	}
}
