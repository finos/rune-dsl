package com.regnosys.rosetta.interpreternew.visitors;

import java.math.BigDecimal;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaStringLiteralImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;

public class RosettaInterpreterParseOperationInterpreter extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterBaseValue interp(ToStringOperation expr, RosettaInterpreterBaseEnvironment env) {
		return null;
		
	}
	
	public RosettaInterpreterBaseValue interp(ToNumberOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		if (argument instanceof RosettaStringLiteralImpl) {
			String string = ((RosettaStringLiteralImpl) argument).getValue();
			BigDecimal number;
			
			try {
				number = new BigDecimal(string);
			} catch (NumberFormatException e) {
	            number = null;
	        }
			
			if (number != null) {
				return new RosettaInterpreterNumberValue(number);
			}
		}
		return new RosettaInterpreterListValue(List.of());
	}
	
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
	
	public RosettaInterpreterBaseValue interp(ToTimeOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		return new RosettaInterpreterListValue(List.of());
		
	}
	
	public RosettaInterpreterBaseValue interp(ToEnumOperation expr, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression argument = expr.getArgument();
		
		return new RosettaInterpreterListValue(List.of());
		
	}
}
