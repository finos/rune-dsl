package com.regnosys.rosetta.interpreternew.visitors;

import java.math.BigDecimal;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;

public class RosettaInterpreterRosettaIntLiteralInterpreter 
	extends RosettaInterpreterConcreteInterpreter {
	
	public RosettaInterpreterBaseValue interp(RosettaIntLiteral expr) {
		return interp(expr, new RosettaInterpreterEnvironment());
	}
	
	public RosettaInterpreterNumberValue interp(RosettaIntLiteral expr, 
			RosettaInterpreterEnvironment env) {
		return new RosettaInterpreterNumberValue(new BigDecimal(expr.getValue()));
	}
}
