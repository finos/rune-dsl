package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
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
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr) {
		boolean ifResult = false;
		
		RosettaExpression ifExression = expr.getIf();
		RosettaExpression ifThen = expr.getIfthen();
		
		RosettaInterpreterValue ifValue = ifExression.accept(visitor); 
		RosettaInterpreterValue ifThenValue = ifThen.accept(visitor);
		
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
		
		if (ifResult) {
			if (RosettaInterpreterErrorValue.errorsExist(ifThenValue)) {
				return createErrors(ifThenValue, 
						"Conditional expression: then is an error value.");
			}
			RosettaInterpreterBaseValue result =  ((RosettaInterpreterBaseValue) 
					ifThenValue).createInstance();
			
			if (expr.isFull()) {
				RosettaExpression elseThen = expr.getElsethen();
				RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
				
				RosettaInterpreterBaseValue elseInstance = 
				((RosettaInterpreterBaseValue) elseThenValue).createInstance();
				
				if (!result.getClass().equals(elseInstance.getClass()) 
						&& !(result 
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
			return result;
			
		} else if (expr.isFull()) {
			RosettaExpression elseThen = expr.getElsethen();
			RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
			
			if (RosettaInterpreterErrorValue.errorsExist(elseThenValue)) {
				return createErrors(elseThenValue, 
						"Conditional expression: else is an error value.");
			}
			
			RosettaInterpreterBaseValue result = ((RosettaInterpreterBaseValue) 
			elseThenValue).createInstance();
			RosettaInterpreterBaseValue ifInstance = ((RosettaInterpreterBaseValue) 
					ifThenValue).createInstance();
			
			if (!result.getClass().equals(ifInstance.getClass()) 
					&& !(result instanceof RosettaInterpreterErrorValue) 
					&& !(ifInstance instanceof RosettaInterpreterErrorValue)) {
				return new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(
								"Conditional expression: "
								+ "then and else "
								+ "need to have the same type."));
			}
			return result;
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
