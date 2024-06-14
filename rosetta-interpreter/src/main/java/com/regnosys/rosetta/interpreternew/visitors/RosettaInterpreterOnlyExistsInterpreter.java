package com.regnosys.rosetta.interpreternew.visitors;

import java.util.*;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterOnlyExistsInterpreter extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterValue interp(RosettaOnlyExistsExpression exp, RosettaInterpreterBaseEnvironment env) {
		
		List<RosettaInterpreterValue> interpretedArgs = new ArrayList<>();
		
		for(RosettaExpression expression : exp.getArgs()) {
			RosettaInterpreterValue val = expression.accept(visitor, env);
			interpretedArgs.add(val);
			System.out.println(val);
			System.out.println();
		}
		
		
		return null;
	}

	
}
