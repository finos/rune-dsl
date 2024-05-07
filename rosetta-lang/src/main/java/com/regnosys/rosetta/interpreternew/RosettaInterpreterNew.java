package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;

public class RosettaInterpreterNew {
	public int Test() {
		return 5;
	}
	
	/**
	 * Simple example interpret function to allow for better understanding of the development workflow
	 * @param expression
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression) {
		if (expression instanceof RosettaIntLiteral) {
			RosettaIntLiteral expr = (RosettaIntLiteral)expression;
			return new RosettaInterpreterIntegerValue(expr.getValue().intValue());
		}
		else
			throw new RosettaInterpreterNewException("Unimplemented operation: " + expression.toString());
	}
}
