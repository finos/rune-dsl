package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaBooleanLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaIntLiteralInterpreter;

public class RosettaInterpreterVisitor extends RosettaInterpreterVisitorBase{

	@Override
	public RosettaInterpreterValue interp(RosettaBooleanLiteral exp) {
		return new RosettaInterpreterRosettaBooleanLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaStringLiteral exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RosettaInterpreterValue interp(RosettaNumberLiteral exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RosettaInterpreterValue interp(RosettaIntLiteral exp) {
		// TODO Auto-generated method stub
		return new RosettaInterpreterRosettaIntLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaPatternLiteral exp) {
		return new RosettaInterpreterErrorValue("Pattern literals are not supported");
	}

	@Override
	public RosettaInterpreterValue interp(ListLiteral exp) {
		// TODO Auto-generated method stub
		return null;
	}

}
