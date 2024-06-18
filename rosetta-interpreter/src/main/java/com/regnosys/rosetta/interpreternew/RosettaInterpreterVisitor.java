package com.regnosys.rosetta.interpreternew;

import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
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
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.interpreter.InterpreterVisitor;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEmptyError;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterLogicalOperationInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterOnlyExistsInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterParseOperationInterpreter;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterComparisonOperationInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterEnumerationInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaArithmeticOperationsInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListOperationsInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterListOperatorInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaBooleanLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaConditionalExpressionInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaConstructorExpressionInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaFeatureCallInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaIntLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaNumberLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterRosettaStringLiteralInterpreter;
import com.regnosys.rosetta.interpreternew.visitors.RosettaInterpreterVariableInterpreter;

public class RosettaInterpreterVisitor extends MinimalEObjectImpl implements InterpreterVisitor {

	
	@Override
	public RosettaInterpreterValue interp(RosettaBooleanLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaBooleanLiteralInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaStringLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaStringLiteralInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaNumberLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaNumberLiteralInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaIntLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaIntLiteralInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaPatternLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterErrorValue(
				new RosettaInterpreterEmptyError("Pattern literals are not supported"));
	}
	
	@Override
	public RosettaInterpreterValue interp(ListLiteral exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListLiteralInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaConditionalExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaConditionalExpressionInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(LogicalOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterLogicalOperationInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(EqualityOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(ComparisonOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterComparisonOperationInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(ArithmeticOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaArithmeticOperationsInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaSymbolReference exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterVariableInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}	

	@Override
	public RosettaInterpreterValue interp(RosettaContainsExpression exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaDisjointExpression exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(JoinOperation exp,
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperationsInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaExistsExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaAbsentExpression exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaOnlyElement exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	
	@Override
	public RosettaInterpreterValue interp(LastOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(FirstOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaCountOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(DistinctOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(ReverseOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(SumOperation exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterListOperatorInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}
	
	@Override
	public RosettaInterpreterEnvironment interp(RosettaEnumeration exp, 
			RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterEnumerationInterpreter().interp(exp,
				(RosettaInterpreterEnvironment) env);
	}

	@Override
	public RosettaInterpreterValue interp(RosettaConstructorExpression exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaConstructorExpressionInterpreter().interp(exp, env);
	}
	
	@Override
	public RosettaInterpreterValue interp(RosettaFeatureCall exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterRosettaFeatureCallInterpreter().interp(exp, 
				(RosettaInterpreterEnvironment) env);
		
	}

	@Override
	public RosettaInterpreterValue interp(RosettaOnlyExistsExpression exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterOnlyExistsInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToStringOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToNumberOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToIntOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToTimeOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToEnumOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToDateOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToDateTimeOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}

	@Override
	public RosettaInterpreterValue interp(ToZonedDateTimeOperation exp, RosettaInterpreterBaseEnvironment env) {
		return new RosettaInterpreterParseOperationInterpreter().interp(exp, env);
	}
}

