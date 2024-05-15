package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;

public class RosettaInterpreterLogicalOperationInterpreter extends RosettaInterpreterConcreteInterpreter{

	public RosettaInterpreterBooleanValue interp(LogicalOperation expr) {
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
			// Idk?? error
		}
		
		if(expr.getOperator().equals("and")) {
			return new RosettaInterpreterBooleanValue(leftBoolean && rightBoolean);
		} else if(expr.getOperator().equals("or")) {
			return new RosettaInterpreterBooleanValue(leftBoolean || rightBoolean);
		} else {
			// Idk?? error
			return null;
		}
	}
}
