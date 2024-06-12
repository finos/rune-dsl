package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;

public class RosettaInterpreterRosettaNumberLiteralInterpreter 
	extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterNumberValue interp(RosettaNumberLiteral exp, 
			RosettaInterpreterEnvironment env) {
		return new RosettaInterpreterNumberValue(exp.getValue());
	}

}
