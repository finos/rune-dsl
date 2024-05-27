package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;

public class RosettaInterpreterRosettaBooleanLiteralInterpreter
	extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaBooleanLiteral expr) {
		return interp(expr, new RosettaInterpreterEnvironment());
	}

	public RosettaInterpreterBooleanValue interp(RosettaBooleanLiteral expr, 
			RosettaInterpreterEnvironment env) {
		return new RosettaInterpreterBooleanValue(expr.isValue());
	}
}
