package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;

import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaBooleanLiteralInterpreter;

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
	public RosettaInterpreterValue interp(LogicalOperation exp) {
		return null;
	}

}
