package com.regnosys.rosetta.interpreternew.visitors;

import java.math.BigDecimal;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.ToDateOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaStringLiteralImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterParseOperationInterpreter extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interpreter method for to-string operation.
	 *
	 * @param expr 		ToStringOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToStringOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		RosettaInterpreterValue result = argument.accept(visitor, env);
		
		if (result instanceof RosettaInterpreterEnumElementValue) {
			return new RosettaInterpreterStringValue(((
					RosettaInterpreterEnumElementValue) result).getValue());
		}
		
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	/**
	 * Interpreter method for to-number operation.
	 *
	 * @param expr 		ToNumberOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToNumberOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			String string = ((RosettaStringLiteralImpl) argument).getValue();
			return checkString(string);
		}
		return new RosettaInterpreterListValue(List.of());
	}
	
	/**
	 * Interpreter method for to-int operation.
	 *
	 * @param expr 		ToIntOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToIntOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			String string = ((RosettaStringLiteralImpl) argument).getValue();
			int number;
			
			try {
				number = new BigDecimal(string).intValueExact();
			} catch (ArithmeticException e) {
	            number = Integer.MIN_VALUE;
	        }
			
			if (number != Integer.MIN_VALUE) {
				return new RosettaInterpreterNumberValue(number);
			}
		}
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	/**
	 * Interpreter method for to-time operation.
	 *
	 * @param expr 		ToTimeOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToTimeOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			return createTime(((RosettaStringLiteralImpl) argument).getValue());	
		}
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	/**
	 * Interpreter method for to-enum operation.
	 *
	 * @param expr 		ToEnumOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterValue interp(ToEnumOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaInterpreterValue argument = expr.getArgument().accept(visitor, env);
		String enumName = expr.getEnumeration().getName();
		String argumentValue =  ((RosettaInterpreterStringValue) argument).getValue();
		
		RosettaInterpreterValue enumValue = env.findValue(enumName);
		
			List<RosettaInterpreterValue> values = ((RosettaInterpreterEnumValue) enumValue).getValues();
			
			for (RosettaInterpreterValue value : values) {
				String name = ((RosettaInterpreterEnumElementValue) value).getValue();
				
				
				if (name.equals(argumentValue)) {
					return value;
				}
			}
		
		
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	/**
	 * Interpreter method for to-date operation.
	 *
	 * @param expr 		ToDateOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToDateOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			return createDate(((RosettaStringLiteralImpl) argument).getValue());	
		}
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	/**
	 * Interpreter method for to-date-time operation.
	 *
	 * @param expr 		TodateTimeOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToDateTimeOperation expr, RosettaInterpreterBaseEnvironment env) {
		// 2024-06-18T01:24:14
		
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			String string = ((RosettaStringLiteralImpl) argument).getValue();
			String[] parts = string.split("T");
			
			if (parts.length != 2) {
				return new RosettaInterpreterListValue(List.of());
			}
			
			RosettaInterpreterValue date = createDate(parts[0]);
			RosettaInterpreterValue time = createTime(parts[1]);
			
			if (date instanceof RosettaInterpreterDateValue
					&& time instanceof RosettaInterpreterTimeValue) {
				return new RosettaInterpreterDateTimeValue((RosettaInterpreterDateValue) date, 
						(RosettaInterpreterTimeValue) time);
			}
		}
		
		return new RosettaInterpreterListValue(List.of());
	}
	
	/**
	 * Interpreter method for to-zoned-date-time operation.
	 *
	 * @param expr 		ToZonedDateTimeOperation to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(ToZonedDateTimeOperation expr, 
			RosettaInterpreterBaseEnvironment env) {
		// 2024-06-18T01:24:14−07:00
		
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			String string = ((RosettaStringLiteralImpl) argument).getValue();
			String[] parts = string.split("T");
			
			if (parts.length != 2) {
				return new RosettaInterpreterListValue(List.of());
			}
			
			int timezoneStartIndex = Math.max(parts[1].lastIndexOf('+'), parts[1].lastIndexOf('-'));
			
			if (timezoneStartIndex == -1) {
				return new RosettaInterpreterListValue(List.of());
			}

			String timeString = parts[1].substring(0, timezoneStartIndex);
			String timezoneString = "UTC" + parts[1].substring(timezoneStartIndex);
			
			RosettaInterpreterValue date = createDate(parts[0]);
			RosettaInterpreterValue time = createTime(timeString);
			
			if (date instanceof RosettaInterpreterDateValue
					&& time instanceof RosettaInterpreterTimeValue) {
				return new RosettaInterpreterZonedDateTimeValue((RosettaInterpreterDateValue) date, 
						(RosettaInterpreterTimeValue) time, 
						new RosettaInterpreterStringValue(timezoneString));
			}
		}
		
		return new RosettaInterpreterListValue(List.of());
	}
	
	/**
	 * Checks if a string can be transformed into a number. 
	 * If yes, returns the interpreted number, else returns empty.
	 *
	 * @param string 		string to be checked
	 * @return 				the interpreted value
	 */
	public RosettaInterpreterBaseValue checkString(String string) {
		BigDecimal number;
		
		try {
			number = new BigDecimal(string);
		} catch (NumberFormatException e) {
            number = null;
        }
		
		if (number != null) {
			return new RosettaInterpreterNumberValue(number);
		}
		
		return new RosettaInterpreterListValue(List.of());
	}
	
	/**
	 * Transforms a string into a time value. If not possible,
	 * returns empty.
	 *
	 * @param string 		string to be transformed
	 * @return 				the interpreted value
	 */
	public RosettaInterpreterBaseValue createTime(String string) {
		String[] strings  = string.split(":");
		
		if (strings.length != 3) {
			return new RosettaInterpreterListValue(List.of());
		}
		
		RosettaInterpreterValue hours = checkString(strings[0]);
		RosettaInterpreterValue minutes = checkString(strings[1]);
		RosettaInterpreterValue seconds = checkString(strings[2]);
		
		if (hours instanceof RosettaInterpreterNumberValue 
				&& minutes instanceof RosettaInterpreterNumberValue
				&& seconds instanceof RosettaInterpreterNumberValue) {
			RosettaInterpreterTimeValue time =  new RosettaInterpreterTimeValue(
					(RosettaInterpreterNumberValue) hours, 
					(RosettaInterpreterNumberValue) minutes, 
					(RosettaInterpreterNumberValue) seconds);
			
			if (time.valid()) {
				return time;
			}
		}
		return new RosettaInterpreterListValue(List.of());
	}
	
	/**
	 * Transforms a string into a date value. If not possible,
	 * returns empty.
	 *
	 * @param string 		string to be transformed
	 * @return 				the interpreted value
	 */
	public RosettaInterpreterBaseValue createDate(String string) {
		String[] strings  = string.split("-");
		
		if (strings.length != 3) {
			return new RosettaInterpreterListValue(List.of());
		}
		
		RosettaInterpreterValue day = checkString(strings[2]);
		RosettaInterpreterValue month = checkString(strings[1]);
		RosettaInterpreterValue year = checkString(strings[0]);
		
		if (day instanceof RosettaInterpreterNumberValue 
				&& month instanceof RosettaInterpreterNumberValue
				&& year instanceof RosettaInterpreterNumberValue) {
			return new RosettaInterpreterDateValue((RosettaInterpreterNumberValue) day, 
					(RosettaInterpreterNumberValue) month, 
					(RosettaInterpreterNumberValue) year);
		}
		return new RosettaInterpreterListValue(List.of());
	}
}
