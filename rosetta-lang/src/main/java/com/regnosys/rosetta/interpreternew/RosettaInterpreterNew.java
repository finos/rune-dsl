package com.regnosys.rosetta.interpreternew;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterNew {
	
	@Inject
	private RosettaInterpreterVisitor visitor;
	
	public int Test() {
		return 5;
	}
	
	/**
	 * Simple example interpret function to allow for better understanding of the development workflow
	 * @param expression
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression) {
		
		// OLD EXAMPLE CODE NOT CONFORMING TO THE VISITOR STYLE
		if (expression instanceof RosettaIntLiteral) {
			RosettaIntLiteral expr = (RosettaIntLiteral)expression;
			return new RosettaInterpreterIntegerValue(expr.getValue().intValue());
		}
		else return expression.accept(visitor);
			
	}
}
