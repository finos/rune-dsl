package com.regnosys.rosetta.interpreternew;


import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Data;

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
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression) {
		return expression.accept(visitor, environment);	
	}
	
	/**
	 * Main interpret function to redirect the interpreter to the correct interpreter.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterValue interp(RosettaExpression expression, 
			RosettaInterpreterBaseEnvironment env) {
		return expression.accept(visitor, env);	
	}

	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow.
	 *
	 * @param func the function to be interpreted
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterBaseEnvironment interp(Function func) {
		return func.accept(visitor, environment);	
	}
	
	/**
	 * Main interpret function to redirect the interpreter to the correct concrete interpreter.
	 *
	 * @param func the function to be interpreted
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterBaseEnvironment interp(Function func, 
			RosettaInterpreterBaseEnvironment env) {
		return func.accept(visitor, env);	
	}
	
	/**
	 * Main interpret function for handling enum declaration.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(RosettaEnumeration expression, 
			RosettaInterpreterBaseEnvironment env) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, env);
		
		return environment;	
	}
	
	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow. It is used in testing for simplification.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(RosettaEnumeration expression) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, environment);
		
		return environment;	
	}
	
	/**
	 * Main interpret function for handling data declaration.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaExpression otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(Data expression, 
			RosettaInterpreterBaseEnvironment env) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, env);
		
		return environment;	
	}
	
	/**
	 * Simple example interpret function to allow for better understanding 
	 * of the development workflow. It is used in testing for simplification.
	 *
	 * @param expression the expression to be interpreted
	 * @return value of RosettaIntLiteral otherwise exception
	 */
	public RosettaInterpreterEnvironment interp(Data expression) {
		
		environment = (RosettaInterpreterEnvironment) expression.accept(visitor, environment);
		
		return environment;	
	}
}
