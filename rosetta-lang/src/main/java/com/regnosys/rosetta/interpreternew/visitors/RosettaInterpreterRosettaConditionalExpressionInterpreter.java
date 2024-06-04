package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConditionalExpressionInterpreter 
extends RosettaInterpreterConcreteInterpreter {
	
	
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr) {
		return interp(expr, new RosettaInterpreterEnvironment());
	}
	
	/**
	 * Interpreter method for Conditional Expressions.
	 *
	 * @param expr RosettaConditionalExpression to be interpreted
	 * @return The interpreted value
	 */
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr,
			RosettaInterpreterBaseEnvironment env) {
		boolean ifResult = false;
		
		RosettaExpression ifExpression = expr.getIf();
		RosettaExpression ifThen = expr.getIfthen();
		
		RosettaInterpreterValue ifValue = ifExpression.accept(visitor, env); 
		RosettaInterpreterValue ifThenValue = ifThen.accept(visitor, env);
		
		RosettaExpression elseThen = null;
		RosettaInterpreterValue elseThenValue = null;
		
		if (ifValue instanceof RosettaInterpreterBooleanValue) {
			ifResult = ((RosettaInterpreterBooleanValue) ifValue).getValue();
		} else if (RosettaInterpreterErrorValue.errorsExist(ifValue)) {
			return createErrors(ifValue, 
					"Conditional expression: condition is an error value.");
		} else {
			return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
					"Conditional expression: condition "
					+ "is not a boolean value."));
		}
		
		if (expr.isFull()) {
			elseThen = expr.getElsethen();
			elseThenValue = elseThen.accept(visitor);
			
			RosettaInterpreterBaseValue ifInstance = 
					((RosettaInterpreterBaseValue) ifThenValue);
			
			RosettaInterpreterBaseValue elseInstance = 
			((RosettaInterpreterBaseValue) elseThenValue);
			
			if (!ifInstance.getClass().equals(elseInstance.getClass()) 
					&& !(ifInstance 
						instanceof RosettaInterpreterErrorValue) 
					&& !(elseInstance 
						instanceof RosettaInterpreterErrorValue)) {
				return new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(
							"Conditional expression: "
							+ "then and else "
							+ "need to have the same type."));
			}
		}
		
		if (ifResult) {
			if (RosettaInterpreterErrorValue.errorsExist(ifThenValue)) {
				return createErrors(ifThenValue, 
						"Conditional expression: then is an error value.");
			}
			
			return ((RosettaInterpreterBaseValue) ifThenValue);
			
		} else if (expr.isFull()) {
			if (RosettaInterpreterErrorValue.errorsExist(elseThenValue)) {
				return createErrors(elseThenValue, 
						"Conditional expression: else is an error value.");
			}
			
			return ((RosettaInterpreterBaseValue) elseThenValue);
		}
		
		return null;
	}
	
	private RosettaInterpreterBaseValue 
	createErrors(RosettaInterpreterValue exp, String message) {
		RosettaInterpreterErrorValue expError = (RosettaInterpreterErrorValue) exp;
		RosettaInterpreterErrorValue newExpError = 
				new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(message));
		
		return RosettaInterpreterErrorValue.merge(List.of(newExpError, expError));
	}
}
