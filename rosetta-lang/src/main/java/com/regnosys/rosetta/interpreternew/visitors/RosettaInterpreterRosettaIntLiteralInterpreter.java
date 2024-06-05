package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;

public class RosettaInterpreterRosettaIntLiteralInterpreter 
	extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaIntLiteral expr) {
		return interp(expr, new RosettaInterpreterEnvironment());
	}
	
	public RosettaInterpreterIntegerValue interp(RosettaIntLiteral expr, 
			RosettaInterpreterEnvironment env) {
		return new RosettaInterpreterIntegerValue(expr.getValue());
	}
}
