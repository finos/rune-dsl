package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterVariableInterpreter {
		
	/**
	 * Interprets a variable, returns the value of it.
	 *
	 * @param exp The RosettaSymbolReference expression to interpret
	 * @param environment RosettaInterpreterBaseEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the variable.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(RosettaSymbolReference exp, 
			RosettaInterpreterBaseEnvironment environment) {
		
		//Search for variable in environment
		RosettaInterpreterValue varValue = environment.findValue(exp.getSymbol().getName());
		
		return varValue;
	}

}
