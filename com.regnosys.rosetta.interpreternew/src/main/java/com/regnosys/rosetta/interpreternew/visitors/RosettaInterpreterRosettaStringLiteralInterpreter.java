package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;

public class RosettaInterpreterRosettaStringLiteralInterpreter 
	extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaStringLiteral expr) {
		return interp(expr, new RosettaInterpreterEnvironment());
	}

	public RosettaInterpreterStringValue interp(RosettaStringLiteral exp, 
			RosettaInterpreterEnvironment env) {
		return new RosettaInterpreterStringValue(exp.getValue());
	}

}
