package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterParseOperationTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Inject 
	ModelHelper modelHelper;
	
	RosettaInterpreterListValue empty = new RosettaInterpreterListValue(List.of());
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(new RosettaInterpreterNumberValue(18), 
			new RosettaInterpreterNumberValue(6), new RosettaInterpreterNumberValue(2024));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(new RosettaInterpreterNumberValue(17), 
			new RosettaInterpreterNumberValue(5), new RosettaInterpreterNumberValue(28));
	
	@Test
	void toStringTest() {
		RosettaModel model = modelHelper.parseRosettaWithNoErrors("enum Foo:\r\n"
				+ "  VALUE1 displayName \"Value 1\"\r\n"
				+ "  VALUE2\r\n"
				+ "\r\n"
				+ "func MyTest:\r\n"
				+ "  output: result string (1..1)\r\n"
				+ "  set result:\r\n"
				+ "    Foo -> VALUE1 to-string");
		RosettaInterpreterEnvironment expectedEnv = 
				new RosettaInterpreterEnvironment();
		expectedEnv.addValue("Foo", 
				new RosettaInterpreterEnumValue("Foo", 
						List.of(new RosettaInterpreterEnumElementValue(
								"Foo", "VALUE1"),new RosettaInterpreterEnumElementValue(
										"Foo", "VALUE2"))));
		RosettaExpression operation = ((FunctionImpl) model.getElements().get(1)).getOperations()
				.get(0).getExpression();
		RosettaInterpreterStringValue result = (RosettaInterpreterStringValue)
				interpreter.interp(operation, expectedEnv);
	    assertEquals(new RosettaInterpreterStringValue("VALUE1"), result);
	}
	
	@Test
	void toSringError() {
		RosettaExpression expr = parser.parseExpression("3.4 to-string");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
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
	void toNumberNotStringError() {
		RosettaExpression expr = parser.parseExpression("3 to-number");
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
	
	@Test
	void toIntNotStringError() {
		RosettaExpression expr = parser.parseExpression("3.4 to-int");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toEnumTest() {
		RosettaModel model = modelHelper.parseRosettaWithNoErrors("enum Foo:\r\n"
				+ "  VALUE1\r\n"
				+ "  VALUE2\r\n"
				+ "\r\n"
				+ "func MyTest:\r\n"
				+ "  output: result Foo (1..1)\r\n"
				+ "  set result:\r\n"
				+ "    \"VALUE1\" to-enum Foo");
		RosettaInterpreterEnvironment expectedEnv = 
				new RosettaInterpreterEnvironment();
		expectedEnv.addValue("Foo", 
				new RosettaInterpreterEnumValue("Foo", 
						List.of(new RosettaInterpreterEnumElementValue(
								"Foo", "VALUE1"),new RosettaInterpreterEnumElementValue(
										"Foo", "VALUE2"))));
		RosettaExpression operation = ((FunctionImpl) model.getElements().get(1)).getOperations()
				.get(0).getExpression();
		RosettaInterpreterEnumElementValue result = (RosettaInterpreterEnumElementValue)
				interpreter.interp(operation, expectedEnv);
	    assertEquals(new RosettaInterpreterEnumElementValue("Foo", "VALUE1"), result);
	}
	
	@Test
	void toEnumError() {
		RosettaModel model = modelHelper.parseRosettaWithNoErrors("enum Foo:\r\n"
				+ "  VALUE1\r\n"
				+ "  VALUE2\r\n"
				+ "\r\n"
				+ "func MyTest:\r\n"
				+ "  output: result Foo (1..1)\r\n"
				+ "  set result:\r\n"
				+ "    \"VALUE3\" to-enum Foo");
		RosettaInterpreterEnvironment expectedEnv = 
				new RosettaInterpreterEnvironment();
		expectedEnv.addValue("Foo", 
				new RosettaInterpreterEnumValue("Foo", 
						List.of(new RosettaInterpreterEnumElementValue(
								"Foo", "VALUE1"),new RosettaInterpreterEnumElementValue(
										"Foo", "VALUE2"))));
		RosettaExpression operation = ((FunctionImpl) model.getElements().get(1)).getOperations()
				.get(0).getExpression();
		RosettaInterpreterValue result = interpreter.interp(operation, expectedEnv);
	    assertEquals(empty, result);
	}
	
	@Test
	void toTimeStringTest() {
		RosettaExpression expr = parser.parseExpression("\"17:05:28\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(time, result);
	}
	
	@Test
	void toTimeError() {
		RosettaExpression expr = parser.parseExpression("\"3.4\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeNotStringError() {
		RosettaExpression expr = parser.parseExpression("3.4 to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeErrorNotValid() {
		RosettaExpression expr = parser.parseExpression("\"17:82:03\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeErrorWrongString() {
		RosettaExpression expr = parser.parseExpression("\"17:28:03:59\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeWrongHoursError() {
		RosettaExpression expr = parser.parseExpression("\"val:05:28\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeWrongMinutesTest() {
		RosettaExpression expr = parser.parseExpression("\"17:05s:28\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toTimeWrongSecondsTest() {
		RosettaExpression expr = parser.parseExpression("\"17:05:2a8\" to-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateStringTest() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18\" to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(date, result);
	}
	
	@Test
	void toDateError() {
		RosettaExpression expr = parser.parseExpression("\"3.4\" to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateWrongDayError() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-day\" to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateWrongMonthError() {
		RosettaExpression expr = parser.parseExpression("\"2024-0m6-18\" to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateWrongYearError() {
		RosettaExpression expr = parser.parseExpression("\"20year24-06-18\" to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateNotStringError() {
		RosettaExpression expr = parser.parseExpression("3 to-date");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateTimeStringTest() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18T17:05:28\" to-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(new RosettaInterpreterDateTimeValue(date, time), result);
	}
	
	@Test
	void toDateTimeError() {
		RosettaExpression expr = parser.parseExpression("\"3.4\" to-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateTimeNotStringError() {
		RosettaExpression expr = parser.parseExpression("3.4 to-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateTimeNotDateError() {
		RosettaExpression expr = parser.parseExpression("\"2024-18T17:05:28\"to-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toDateTimeNotTimeError() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18T17\" to-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toZonedDateTimeStringTest() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18T17:05:28-07:00\" to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(new RosettaInterpreterZonedDateTimeValue(date, time, 
				new RosettaInterpreterStringValue("UTC-07:00")), result);
	}
	
	@Test
	void toZonedDateTimeError1() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18T17:05:28\" to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toZonedDateTimeError2() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18\" to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toZonedDateTimeNotStringError() {
		RosettaExpression expr = parser.parseExpression("3 to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toZonedDateTimeNotDateError() {
		RosettaExpression expr = parser.parseExpression("\"18T17:05:28-07:00\" to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
	
	@Test
	void toZonedDateTimeNotTimeError() {
		RosettaExpression expr = parser.parseExpression("\"2024-06-18T17:28-07:00\" to-zoned-date-time");
		RosettaInterpreterValue result = interpreter.interp(expr);
		
		assertEquals(empty, result);
	}
}
