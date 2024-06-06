package com.regnosys.rosetta.interpreternew;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Function;

public class RosettaInterpreterNew {
	
	@Inject
	private RosettaInterpreterVisitor visitor;
	
	@Inject
	private RosettaInterpreterEnvironment environment;
	
	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression) {
		return expression.accept(visitor, environment);	
	}
	
	public RosettaInterpreterValue interp(RosettaExpression expression, 
			RosettaInterpreterBaseEnvironment env) {
		return expression.accept(visitor, env);	
	}
	
	public RosettaInterpreterBaseEnvironment interp(Function func) {
		return func.accept(visitor, environment);	
	}
	
	public RosettaInterpreterBaseEnvironment interp(Function func, 
			RosettaInterpreterBaseEnvironment env) {
		return func.accept(visitor, env);	
	}
	
	
}
