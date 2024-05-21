package com.regnosys.rosetta.interpreternew.visitors;

import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;

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
		
		Boolean errorLeftSide = RosettaInterpreterErrorValue.errorsExist(leftInterpreted);
		Boolean errorRightSide = RosettaInterpreterErrorValue.errorsExist(leftInterpreted);
		
		if(leftInterpreted instanceof RosettaInterpreterBooleanValue 
				&& rightInterpreted instanceof RosettaInterpreterBooleanValue) {
			leftBoolean = ((RosettaInterpreterBooleanValue) leftInterpreted).getValue();
			rightBoolean = ((RosettaInterpreterBooleanValue) rightInterpreted).getValue();
		} else if(errorLeftSide || errorRightSide) {
			// Check for errors in the left or right side of the binary operation
			
			if(errorLeftSide == false) return (RosettaInterpreterErrorValue) rightInterpreted;
			else if(errorRightSide == false) return (RosettaInterpreterErrorValue) leftInterpreted;
			else {
				// There were errors on both sides => Combine the error messages
				return RosettaInterpreterErrorValue.merge(List.of(leftInterpreted, rightInterpreted));
			}
		} else { // The interpreted value was not an error, but something other than a boolean
			return makeNewError(leftInterpreted, rightInterpreted); 
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
	private RosettaInterpreterErrorValue makeNewError(RosettaInterpreterValue left, RosettaInterpreterValue right) {
		RosettaInterpreterErrorValue newError = new RosettaInterpreterErrorValue();
		if(!(left instanceof RosettaInterpreterBooleanValue)) {
			newError.addError(new RosettaInterpreterError("Logical Operation: Leftside is not of type Boolean"));
		}
		if(!(right instanceof RosettaInterpreterBooleanValue)) {
			newError.addError(new RosettaInterpreterError("Logical Operation: Rightside is not of type Boolean"));
		}
		
		return newError;
	}
}
