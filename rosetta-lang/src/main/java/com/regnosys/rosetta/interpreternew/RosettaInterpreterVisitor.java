package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.expression.ReverseOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
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
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterLogicalOperationInterpreter;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterComparisonOperationInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaArithmeticOperationsInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListOperationsInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListOperatorInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaBooleanLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaConditionalExpressionInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaConstructorExpressionInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaIntLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaNumberLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaStringLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterVariableInterpreter;

public class RosettaInterpreterVisitor extends RosettaInterpreterVisitorBase {

	@Override
	public RosettaInterpreterValue interp(RosettaBooleanLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaBooleanLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaBooleanLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaStringLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaStringLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaStringLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaNumberLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaNumberLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaNumberLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaIntLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaIntLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaIntLiteralInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaPatternLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaPatternLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterErrorValue(
				new RosettaInterpreterError("Pattern literals are not supported"));
	}

	@Override
	public RosettaInterpreterValue interp(ListLiteral exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(ListLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListLiteralInterpreter().interp(exp);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaConditionalExpression exp) {
		return new RosettaInterpreterRosettaConditionalExpressionInterpreter().interp(exp);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaConditionalExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaConditionalExpressionInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(LogicalOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(LogicalOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterLogicalOperationInterpreter().interp(exp);
	}
	
	@Override
	public RosettaInterpreterValue interp(EqualityOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(EqualityOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp);
	}

	@Override
	public RosettaInterpreterValue interp(ComparisonOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(ComparisonOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp, env);
	}
	
	@Override
	public RosettaInterpreterValue interp(ArithmeticOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(ArithmeticOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaArithmeticOperationsInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaSymbolReference exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(RosettaSymbolReference exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterVariableInterpreter().interp(exp, env);
	}	

	@Override
	public RosettaInterpreterValue interp(RosettaContainsExpression exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(RosettaContainsExpression exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaDisjointExpression exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(RosettaDisjointExpression exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(JoinOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(JoinOperation exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaExistsExpression exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaExistsExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}


	@Override
	public RosettaInterpreterValue interp(RosettaAbsentExpression exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaAbsentExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaOnlyElement exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaOnlyElement exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}
	

	@Override
	public RosettaInterpreterValue interp(LastOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(LastOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}
	
	@Override
	public RosettaInterpreterValue interp(FirstOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(FirstOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaCountOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaCountOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(DistinctOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(DistinctOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ReverseOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(ReverseOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(SumOperation exp) {
		return interp(exp, new RosettaInterpreterEnvironment());
	}

	@Override
	public RosettaInterpreterValue interp(SumOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaConstructorExpression exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaConstructorExpressionInterpreter().interp(exp, env);
	}
}
