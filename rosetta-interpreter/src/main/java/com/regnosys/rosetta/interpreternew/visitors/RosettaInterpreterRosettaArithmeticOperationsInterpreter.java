package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.rosetta.model.lib.RosettaNumber;

public class RosettaInterpreterRosettaArithmeticOperationsInterpreter 
					extends RosettaInterpreterConcreteInterpreter {
	
	
	/**
	 * Interprets an arithmetic operation, evaluating the operation between the two terms.
	 *
	 * @param expr The ArithmeticOperation expression to interpret
	 * @return If no errors are encountered, a RosettaInterpreterNumberValue or
	 * 		   RosettaInterpreterStringValue representing
	 * 		   the result of the arithmetic/concatenation operation.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(ArithmeticOperation expr,
			RosettaInterpreterEnvironment env) {

		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();
		RosettaInterpreterValue leftInterpreted = left.accept(visitor, env);
		RosettaInterpreterValue rightInterpreted = right.accept(visitor, env); 
		
		if (!(leftInterpreted instanceof RosettaInterpreterNumberValue 
				|| leftInterpreted instanceof RosettaInterpreterStringValue) 
				|| !(rightInterpreted instanceof RosettaInterpreterNumberValue 
				|| rightInterpreted instanceof RosettaInterpreterStringValue)) {
			
			// Check for errors in the left or right side of the binary operation
			RosettaInterpreterErrorValue leftErrors = 
					checkForErrors(leftInterpreted, "Leftside");
			RosettaInterpreterErrorValue rightErrors = 
					checkForErrors(rightInterpreted, "Rightside");
			return RosettaInterpreterErrorValue.merge(List.of(leftErrors, rightErrors));
		}	
		
		if (leftInterpreted instanceof RosettaInterpreterStringValue
				&& rightInterpreted instanceof RosettaInterpreterStringValue) {
			String leftString = ((RosettaInterpreterStringValue) leftInterpreted)
					.getValue();
			String rightString = ((RosettaInterpreterStringValue) rightInterpreted)
					.getValue();
			if (expr.getOperator().equals("+")) {
				return new RosettaInterpreterStringValue(leftString + rightString);
				}
			else {
				return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
				"The terms are strings but the operation "
				+ "is not concatenation: not implemented"));
			}
		} else if (leftInterpreted instanceof RosettaInterpreterNumberValue
				&& rightInterpreted instanceof RosettaInterpreterNumberValue) {
			RosettaNumber leftNumber = ((RosettaInterpreterNumberValue) leftInterpreted).getValue();
			RosettaNumber rightNumber = ((RosettaInterpreterNumberValue) rightInterpreted).getValue();
			
			if (expr.getOperator().equals("+")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.add(rightNumber)).bigDecimalValue());
			} else if (expr.getOperator().equals("-")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.subtract(rightNumber)).bigDecimalValue());
			} else if (expr.getOperator().equals("*")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.multiply(rightNumber)).bigDecimalValue());
			} else {
				// Division by 0 is not allowed
				if (rightNumber.floatValue() == 0.0) {
					return new RosettaInterpreterErrorValue(
							new RosettaInterpreterError(
							"Division by 0 is not allowed"));
				}
				return new RosettaInterpreterNumberValue((leftNumber
						.divide(rightNumber)).bigDecimalValue());
			}
		} else {
			return new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
				"The terms of the operation are neither both strings nor both numbers"));
		}	
	}
	
	
	/**
	 * Helper method that takes an interpretedValue and a string,
	 * and returns the correct error which
	 * interpretedValue causes, if any.
	 *
	 * @param interpretedValue The interpreted value which we check for errors
	 * @param side String containing either "Leftside" or "Rightside", 
	 *        purely for clearer error messages
	 * @return The correct RosettaInterpreterErrorValue, or "null" 
	 *         if the interpretedValue does not cause an error
	 */
	private RosettaInterpreterErrorValue checkForErrors(
			RosettaInterpreterValue interpretedValue, String side) {
		if  (interpretedValue instanceof RosettaInterpreterNumberValue 
				|| interpretedValue instanceof RosettaInterpreterStringValue) {
			// If the value satisfies the type conditions, we return an empty 
			// error value so that the merger has two error values to merge
			return new RosettaInterpreterErrorValue();
		}
		
		else if (RosettaInterpreterErrorValue.errorsExist(interpretedValue)) {
			// The interpreted value was an error so we propagate it
			return (RosettaInterpreterErrorValue) interpretedValue;
		} else {
			// The interpreted value was not an error,
			// but something other than a string or number
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"Arithmetic Operation: " + side 
							+ " is not of type Number/String"));
		}
	}
}

