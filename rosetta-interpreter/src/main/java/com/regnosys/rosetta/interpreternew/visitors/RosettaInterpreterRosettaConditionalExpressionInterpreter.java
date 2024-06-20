package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConditionalExpressionInterpreter 
extends RosettaInterpreterConcreteInterpreter {
	
	
	/**
	 * Interpreter method for Conditional Expressions.
	 *
	 * @param expr RosettaConditionalExpression to be interpreted
	 * @return The interpreted value
	 */
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr,
			RosettaInterpreterEnvironment env) {
		boolean ifResult = false;
		
		RosettaExpression ifExpression = expr.getIf();
		RosettaExpression ifThen = expr.getIfthen();
		RosettaExpression elseThen = expr.getElsethen();
		
		RosettaInterpreterValue ifValue = ifExpression.accept(visitor, env); 

		if (ifValue instanceof RosettaInterpreterBooleanValue) {
			ifResult = ((RosettaInterpreterBooleanValue) ifValue).getValue();
		} else if (RosettaInterpreterErrorValue.errorsExist(ifValue)) {
			return createErrors(ifValue, 
					"Conditional expression: condition is an error value.");
		} else {
			return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
					"Conditional expression: condition "
					+ "is not a boolean value.", expr));
		}
		
		if (expr.isFull()) {
			elseThen = expr.getElsethen();
			RosettaInterpreterValue elseThenValue = elseThen.accept(visitor, env);
			RosettaInterpreterValue ifThenValue = ifThen.accept(visitor, env);
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
							+ "need to have the same type.", expr));
			}
		}
		
		if (ifResult) { 
			RosettaInterpreterValue ifThenValue = ifThen.accept(visitor, env);
			if (RosettaInterpreterErrorValue.errorsExist(ifThenValue)) {
				return createErrors(ifThenValue, 
						"Conditional expression: then is an error value.");
			}
			
			return ((RosettaInterpreterBaseValue) ifThenValue);
			
		} else if (expr.isFull()) {
			RosettaInterpreterValue elseThenValue = elseThen.accept(visitor, env);
			if (RosettaInterpreterErrorValue.errorsExist(elseThenValue)) {
				return createErrors(elseThenValue, 
						"Conditional expression: else is an error value.");
			}
			
			return ((RosettaInterpreterBaseValue) elseThenValue);
		} else {
			// Else branch should be evaluated but it does not exist
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
					"Else branch should be evaluated but does not exist", expr));
		}
	}
	
	private RosettaInterpreterBaseValue 
	createErrors(RosettaInterpreterValue exp, String message) {
		RosettaInterpreterErrorValue expError = (RosettaInterpreterErrorValue) exp;
		RosettaInterpreterErrorValue newExpError = 
				new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(message, exp));
		
		return RosettaInterpreterErrorValue.merge(List.of(newExpError, expError));
	}
}
