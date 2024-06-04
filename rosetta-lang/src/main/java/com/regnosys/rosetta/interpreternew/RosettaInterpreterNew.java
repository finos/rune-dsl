package com.regnosys.rosetta.interpreternew;


import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

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
	
	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(RosettaEnumeration expression, 
			RosettaInterpreterBaseEnvironment env) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, env);
		
		return environment;	
	}
	
	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(RosettaEnumeration expression) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, environment);
		
		return environment;	
	}
}
