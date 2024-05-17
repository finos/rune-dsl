package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConditionalExpressionInterpreter extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaConditionalExpression expr) {
		boolean ifResult = false;
		
		RosettaExpression if_ = expr.getIf();
		RosettaExpression ifThen = expr.getIfthen();
		RosettaExpression elseThen = expr.getElsethen();
		
		RosettaInterpreterValue ifValue = if_.accept(visitor); 
		RosettaInterpreterValue ifThenValue = ifThen.accept(visitor);
		RosettaInterpreterValue elseThenValue = elseThen.accept(visitor);
		
		if (ifValue instanceof RosettaInterpreterBooleanValue) {
			ifResult = ((RosettaInterpreterBooleanValue) ifValue).getValue();
		} else {
			// error
		}
		
		if (ifResult == true) {
			
		}
		
		return null;
	}
}
