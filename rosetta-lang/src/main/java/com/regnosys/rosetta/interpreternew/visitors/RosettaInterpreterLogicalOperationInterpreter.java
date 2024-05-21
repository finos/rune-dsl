package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterLogicalOperationInterpreter 
	extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interpreter method for Logical Operations.
	 *
	 * @param expr LogicalOperaation to be interpreted
	 * @return The interpreted value
	 */
	public RosettaInterpreterBaseValue interp(LogicalOperation expr) {
		boolean leftBool = false;
		boolean rightBool = false;
		
		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();
		RosettaInterpreterValue leftInterpreted = left.accept(visitor);
		RosettaInterpreterValue rightInterpreted = right.accept(visitor);
		
		if (leftInterpreted instanceof RosettaInterpreterBooleanValue 
				&& rightInterpreted instanceof RosettaInterpreterBooleanValue) {
			leftBool = ((RosettaInterpreterBooleanValue) leftInterpreted).getValue();
			rightBool = ((RosettaInterpreterBooleanValue) rightInterpreted).getValue();
		} else {
			// Check for errors in the left or right side of the binary operation
			RosettaInterpreterErrorValue leftErrors = 
					checkForErrors(leftInterpreted, "Leftside");
			RosettaInterpreterErrorValue rightErrors = 
					checkForErrors(rightInterpreted, "Rightside");
			
			return RosettaInterpreterErrorValue.merge(List.of(leftErrors, rightErrors));
		}
			
		if (expr.getOperator().equals("and")) {
			return new RosettaInterpreterBooleanValue(leftBool && rightBool);
		} else if (expr.getOperator().equals("or")) {
			return new RosettaInterpreterBooleanValue(leftBool || rightBool);
		} else {
			// Wrong logical operator -> only "and" / "or" supported
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
					"Logical Operation: Wrong operator "
					+ "- only 'and' / 'or' supported"));
		}
	}
	
	/**
	 * Helper method that takes an interpretedValue and a string
	 * , and returns the correct error which
	 * that interpretedValue causes, if any.
	 *
	 * @param interpretedValue The interpreted value which we check for errors
	 * @param side String containing either "Leftside" or "Rightside", 
	 *        purely for clearer error messages
	 * @return The correct RosettaInterpreterErrorValue, or "null" 
	 *         if the interpretedValue does not cause an error
	 */
	private RosettaInterpreterErrorValue checkForErrors(
			RosettaInterpreterValue interpretedValue, String side) {
		if (interpretedValue instanceof RosettaInterpreterBooleanValue) {
			// No errors found.
			// I return an error value without any errors in its list,
			// So that I can still use the merge method with 2 elements
			return new RosettaInterpreterErrorValue();
		} else if (RosettaInterpreterErrorValue.errorsExist(interpretedValue)) {
			// The interpreted value was an error so we propagate it
			return (RosettaInterpreterErrorValue) interpretedValue;
		} else {
			// The interpreted value was not an error,
			// but something other than a boolean
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"Logical Operation: " + side + 
							" is not of type Boolean"));
		}
	}
}
