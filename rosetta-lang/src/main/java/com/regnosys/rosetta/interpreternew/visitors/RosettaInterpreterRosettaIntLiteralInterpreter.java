package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;

public class RosettaInterpreterRosettaIntLiteralInterpreter 
	extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterIntegerValue interp(RosettaIntLiteral expr) {
		return new RosettaInterpreterIntegerValue(expr.getValue());
	}
}
