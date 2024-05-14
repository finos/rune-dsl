package com.regnosys.rosetta.interpreternew;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;

public class RosettaInterpreterNew {
	
	@Inject
	private RosettaInterpreterVisitor visitor;
	
	/**
	 * Simple example interpret function to allow for better understanding of the development workflow
	 * @param expression
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression) {
		return expression.accept(visitor);	
	}
}
