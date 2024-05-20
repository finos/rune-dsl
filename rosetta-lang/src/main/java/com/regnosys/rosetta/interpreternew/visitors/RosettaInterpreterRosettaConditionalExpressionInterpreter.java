package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
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
		} else if (ifValue instanceof RosettaInterpreterErrorValue) {
			return createErrors(ifValue, "Conditional expression: condition is an error value.");
		} else {
			return new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: condition is not a boolean value."));
		}
		
		if (ifResult == true) {
			RosettaInterpreterBaseValue result =  checkInstance(ifThenValue, false);
			
			if (expr.isFull()) {
				RosettaExpression elseThen = expr.getElsethen();
				RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
				
				RosettaInterpreterBaseValue elseInstance = checkInstance(elseThenValue, true);
				
				if (!result.getClass().equals(elseInstance.getClass())) {
					return new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: consequent and alternative need to have the same type."));
				}
			}
			return result;
		} else if (expr.isFull()) {
			RosettaExpression elseThen = expr.getElsethen();
			RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
			
			RosettaInterpreterBaseValue result = checkInstance(elseThenValue, true);
			RosettaInterpreterBaseValue ifInstance = checkInstance(ifThenValue, true);
			
			if (!result.getClass().equals(ifInstance.getClass())) {
				return new RosettaInterpreterErrorValue(new RosettaInterpreterError("Conditional expression: consequent and alternative need to have the same type."));
			}
			return result;
		}
		
		return null;
	}
	
	private RosettaInterpreterBaseValue checkInstance(RosettaInterpreterValue expr, boolean branch) {
		RosettaInterpreterBaseValue result = null;
		String message = null;
		
		if (branch == false) {
			message = "consequent";
		} else {
			message = "alternative";
		}
		
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
		} else if (expr instanceof RosettaInterpreterErrorValue) {
			result = createErrors(expr, "Conditional expression: " + message + " is an error value");
		}
		
		return result;
	}
	
	private RosettaInterpreterBaseValue createErrors(RosettaInterpreterValue exp, String message) {
		RosettaInterpreterErrorValue expError = (RosettaInterpreterErrorValue) exp;
		RosettaInterpreterErrorValue newExpError = new RosettaInterpreterErrorValue(new RosettaInterpreterError(message));
		
		newExpError.addAllErrors(expError);
		return newExpError;
	}
}
