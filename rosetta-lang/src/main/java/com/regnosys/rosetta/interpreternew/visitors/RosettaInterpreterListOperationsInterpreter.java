package com.regnosys.rosetta.interpreternew.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterListOperationsInterpreter
	extends RosettaInterpreterConcreteInterpreter {


	/**
	 * Interprets a rosetta contains expression.
	 * Checks if the right element, which may be a single element or a list,
	 * is contained within the left list
	 *
	 * @param exp - expression to evaluate
	 * @return value of contains expression
	 */
	public RosettaInterpreterValue interp(RosettaContainsExpression exp) {
		RosettaExpression leftExp = exp.getLeft();
		RosettaExpression rightExp = exp.getRight();
		
		RosettaInterpreterValue leftVal = leftExp.accept(visitor);
		RosettaInterpreterValue rightVal = rightExp.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(leftVal, rightVal)) {
			return RosettaInterpreterErrorValue.merge(leftVal, rightVal);
		}
		
		if (RosettaInterpreterBaseValue.valueStream(rightVal).count() < 1L
				|| RosettaInterpreterBaseValue.valueStream(leftVal).count() < 1L) {
			return new RosettaInterpreterBooleanValue(false);
		}
		
		HashSet<RosettaInterpreterValue> leftValueSet = new HashSet<>(
				RosettaInterpreterBaseValue.valueStream(leftVal)
				.collect(Collectors.toList()));
		
		boolean contains = RosettaInterpreterBaseValue.valueStream(rightVal)
				.allMatch(x -> leftValueSet.contains(x));
		
		return new RosettaInterpreterBooleanValue(contains);
	}
	
	/**
	 * Interprets a rosetta disjoint expression.
	 * Checks if the right element, which may be a single element or a list,
	 * is not contained within the left list
	 *
	 * @param exp - expression to evaluate
	 * @return value of contains expression
	 */
	public RosettaInterpreterValue interp(RosettaDisjointExpression exp) {
		RosettaExpression leftExp = exp.getLeft();
		RosettaExpression rightExp = exp.getRight();
		
		RosettaInterpreterValue leftVal = leftExp.accept(visitor);
		RosettaInterpreterValue rightVal = rightExp.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(leftVal, rightVal)) {
			return RosettaInterpreterErrorValue.merge(leftVal, rightVal);
		}
		
		if (RosettaInterpreterBaseValue.valueStream(rightVal).count() < 1L 
				|| RosettaInterpreterBaseValue.valueStream(leftVal).count() < 1L) {
			return new RosettaInterpreterBooleanValue(true);
		}
		
		HashSet<RosettaInterpreterValue> rightValueSet = new HashSet<>(
				RosettaInterpreterBaseValue.valueStream(rightVal)
				.collect(Collectors.toList()));
		
		boolean notContains = RosettaInterpreterBaseValue.valueStream(leftVal)
				.allMatch(x -> !rightValueSet.contains(x));
		
		return new RosettaInterpreterBooleanValue(notContains);
	}

	/**
	 * Interprets a join operation.
	 * Join takes as input a list of strings and a delimiter
	 * Then returns a single string of the strings concatenated with the join operator
	 *
	 * @param exp - join operation to interpret
	 * @return concatenated string
	 */
	public RosettaInterpreterValue interp(JoinOperation exp) {
		RosettaExpression stringsExp = exp.getLeft();
		RosettaExpression delimExp = exp.getRight();
		
		RosettaInterpreterValue stringsVal = stringsExp.accept(visitor);
		RosettaInterpreterValue delimVal = delimExp.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(stringsVal, delimVal)) {
			return RosettaInterpreterErrorValue.merge(stringsVal, delimVal);
		}
		
		boolean allStrings = RosettaInterpreterBaseValue.valueStream(stringsVal)
				.allMatch(x -> x instanceof RosettaInterpreterStringValue);
		
		if (!allStrings) {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("The list of values for a join "
							+ "operation must be a list of strings"));
		}
		if (!(delimVal instanceof RosettaInterpreterStringValue)) {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("The delimiter for a join"
							+ " operation must be a string"));
		}
		
		if (RosettaInterpreterBaseValue.valueStream(stringsVal)
				.count() < 1L) {
			return new RosettaInterpreterStringValue("");
		}
		
		String delimString = ((RosettaInterpreterStringValue)delimVal).getValue();
		List<String> texts = RosettaInterpreterBaseValue.valueStream(stringsVal)
				.map(x -> ((RosettaInterpreterStringValue)x).getValue())
				.collect(Collectors.toList());
		
		String result = String.join(delimString, texts);
		
		return new RosettaInterpreterStringValue(result);
	}
		
}
