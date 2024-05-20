package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterLogicalOperationInterpreter extends RosettaInterpreterConcreteInterpreter{

	/**
	 * Interpreter method for Logical Operations.
	 * 
	 * @param expr LogicalOperaation to be interpreted
	 * @return The interpreted value
	 */
	public RosettaInterpreterBaseValue interp(LogicalOperation expr) {
		boolean leftBoolean = false;
		boolean rightBoolean = false;
		
		
		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();
		RosettaInterpreterValue leftInterpreted = left.accept(visitor);
		RosettaInterpreterValue rightInterpreted = right.accept(visitor);
		
		if(leftInterpreted instanceof RosettaInterpreterBooleanValue 
				&& rightInterpreted instanceof RosettaInterpreterBooleanValue) {
			leftBoolean = ((RosettaInterpreterBooleanValue) leftInterpreted).getValue();
			rightBoolean = ((RosettaInterpreterBooleanValue) rightInterpreted).getValue();
		} else {
			// Check for errors in the left or right side of the binary operation
			RosettaInterpreterErrorValue leftSideCheck = checkForErrors(leftInterpreted, "Leftside");
			RosettaInterpreterErrorValue rightSideCheck = checkForErrors(rightInterpreted, "Rightside");
			
			// Null means there were no errors on that side
			if(leftSideCheck == null) return rightSideCheck;
			else if(rightSideCheck == null) return leftSideCheck;
			else { 
				// There were errors on both sides => Combine the error messages
				RosettaInterpreterErrorValue newErrorValue = new RosettaInterpreterErrorValue();
				newErrorValue.addAllErrors(leftSideCheck);
				newErrorValue.addAllErrors(rightSideCheck);
				return newErrorValue;
			}
		}
		
		if(expr.getOperator().equals("and")) {
			return new RosettaInterpreterBooleanValue(leftBoolean && rightBoolean);
		} else if(expr.getOperator().equals("or")) {
			return new RosettaInterpreterBooleanValue(leftBoolean || rightBoolean);
		} else {
			// Wrong logical operator -> only "and" / "or" supported
			return new RosettaInterpreterErrorValue(new RosettaInterpreterError("Logical Operation: Wrong operator - only 'and' / 'or' supported"));
		}
	}
	
	/**
	 * Helper method that takes an interpretedValue and a string, and returns the correct error which
	 * that interpretedValue causes, if any.
	 * 
	 * @param interpretedValue The interpreted value which we check for errors
	 * @param side String containing either "Leftside" or "Rightside", purely for clearer error messages
	 * @return The correct RosettaInterpreterErrorValue, or "null" if the interpretedValue does not cause an error
	 */
	private RosettaInterpreterErrorValue checkForErrors(RosettaInterpreterValue interpretedValue, String side) {
		if(interpretedValue instanceof RosettaInterpreterBooleanValue) {
			// No errors found
			return null;
		} else if(interpretedValue instanceof RosettaInterpreterErrorValue) {
			// The interpreted value was an error (so we need to add a new error message to the existing ones)
			return (RosettaInterpreterErrorValue) interpretedValue;
		} else {
			// The interpreted value was not an error, but something other than a boolean
			return new RosettaInterpreterErrorValue(new RosettaInterpreterError("Logical Operation: " + side + " is not of type Boolean"));
		}
	}
}
