package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterComparisonOperationInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaBooleanLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaIntLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaNumberLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaStringLiteralInterpreter;

public class RosettaInterpreterVisitor extends RosettaInterpreterVisitorBase{

	@Override
	public RosettaInterpreterValue interp(RosettaBooleanLiteral exp) {
		return new RosettaInterpreterRosettaBooleanLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaStringLiteral exp) {
		return new RosettaInterpreterRosettaStringLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaNumberLiteral exp) {
		return new RosettaInterpreterRosettaNumberLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaIntLiteral exp) {
		return new RosettaInterpreterRosettaIntLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaPatternLiteral exp) {
		return new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Pattern literals are not supported"));
	}

	@Override
	public RosettaInterpreterValue interp(ListLiteral exp) {
		return new RosettaInterpreterListLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(EqualityOperation exp) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(ComparisonOperation exp) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaContainsExpression exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(RosettaDisjointExpression exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(JoinOperation exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(RosettaExistsExpression exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(RosettaAbsentExpression exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(RosettaCountOperation exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(FirstOperation exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RosettaInterpreterValue interp(LastOperation exp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
