package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterFunctionValue;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;

public class RosettaInterpreterFunctionDeclarationInterpreter extends 
	RosettaInterpreterConcreteInterpreter {
	
	/**
	 * Interprets a function declaration, and adds it to the environment.
	 *
	 * @param func the function to be interpreter
	 * @return the list value it represents
	 */
	public RosettaInterpreterEnvironment interp(Function func, 
			RosettaInterpreterEnvironment env) {
		RosettaInterpreterFunctionValue val = 
				new RosettaInterpreterFunctionValue((FunctionImpl) func);
		env.addValue(func.getName(), val);
		return env;
	}
}