package com.regnosys.rosetta.interpreternew.visitors;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitor;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;

public class RosettaInterpreterVariableInterpreter {
	
	@Inject
	protected RosettaInterpreterVisitor visitor = new RosettaInterpreterVisitor();
	
	
	
	/**
	 * Interprets a variable, returns the value of it.
	 *
	 * @param exp The RosettaSymbolReference expression to interpret
	 * @param env RosettaInterpreterBaseEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the variable.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(RosettaSymbolReference exp, 
			RosettaInterpreterBaseEnvironment env) {
		
		if (exp.getSymbol() instanceof FunctionImpl) {
			return (new RosettaInterpreterFunctionInterpreter())
					.interp((FunctionImpl) exp.getSymbol(), exp.getRawArgs(), env);
		}
		
		return interp(exp.getSymbol().getName(), env);
	}
		
	/**
	 * Interprets a variable, returns the value of it.
	 *
	 * @param varName The name of the variable to interpret
	 * @param env RosettaInterpreterEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the variable.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(String varName, 
			RosettaInterpreterBaseEnvironment env) {
		
		//Search for variable in environment
		RosettaInterpreterValue varValue = env.findValue(varName);
		
		return varValue;
	}
	
	

}
