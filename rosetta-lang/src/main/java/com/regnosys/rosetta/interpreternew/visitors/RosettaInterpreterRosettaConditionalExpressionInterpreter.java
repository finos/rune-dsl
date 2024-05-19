package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConditionalExpressionInterpreter extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr) {
		boolean ifResult = false;
		
		RosettaExpression if_ = expr.getIf();
		RosettaExpression ifThen = expr.getIfthen();
		
		RosettaInterpreterValue ifValue = if_.accept(visitor); 
		RosettaInterpreterValue ifThenValue = ifThen.accept(visitor);
		
		if (ifValue instanceof RosettaInterpreterBooleanValue) {
			ifResult = ((RosettaInterpreterBooleanValue) ifValue).getValue();
		} else {
			// error
		}
		
		if (ifResult == true) {
			return checkInstance(ifThenValue);
		} else if (expr.isFull()) {
			RosettaExpression elseThen = expr.getElsethen();
			RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
			
			return checkInstance(elseThenValue);
		}
		
		return null;
	}
	
	private RosettaInterpreterBaseValue checkInstance(RosettaInterpreterValue expr) {
		RosettaInterpreterBaseValue result = null;
		
		if (expr instanceof RosettaInterpreterBooleanValue) {
			result = new RosettaInterpreterBooleanValue(((RosettaInterpreterBooleanValue) expr).getValue());
		} else if (expr instanceof RosettaInterpreterIntegerValue) {
			result = new RosettaInterpreterIntegerValue(((RosettaInterpreterIntegerValue) expr).getValue());
		} else if (expr instanceof RosettaInterpreterNumberValue) {
			result = new RosettaInterpreterNumberValue(((RosettaInterpreterNumberValue) expr).getValue());
		} else if (expr instanceof RosettaInterpreterStringValue) {
			result = new RosettaInterpreterStringValue(((RosettaInterpreterStringValue) expr).getValue());
		} else if (expr instanceof RosettaInterpreterListValue) {
			result = new RosettaInterpreterListValue(((RosettaInterpreterListValue) expr).getExpressions());
		} else {
			// error
		}
		
		return result;
	}
}
