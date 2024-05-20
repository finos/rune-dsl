package com.regnosys.rosetta.interpreternew.visitors;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
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
		
		if (RosettaInterpreterBaseValue.valueStream(rightVal).count() < 1L ||
				RosettaInterpreterBaseValue.valueStream(leftVal).count() < 1L) {
			return new RosettaInterpreterBooleanValue(false);
		}
		
		HashSet<RosettaInterpreterValue> leftValueSet = new HashSet<>(
				RosettaInterpreterBaseValue.valueStream(leftVal)
				.collect(Collectors.toList()));
		
		boolean contains = RosettaInterpreterBaseValue.valueStream(rightVal)
				.allMatch(x -> leftValueSet.contains(x));
		
		return new RosettaInterpreterBooleanValue(contains);
	}
		
}
