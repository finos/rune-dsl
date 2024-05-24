package com.regnosys.rosetta.interpreternew.visitors;

import java.math.BigInteger;
import java.util.stream.Collectors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterListOperatorInterpreter
	extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interprets an exists operation.
	 * If the argument of the expression is an error, it returns it.
	 * Otherwise, it checks to see if the interpreted argument
	 * is of single or multiple cardinality.
	 *
	 * @param exp Exists operation to interpret
	 * @return Boolean indicating if the argument exists or not
	 */
	public RosettaInterpreterValue interp(RosettaExistsExpression exp) {
		RosettaExpression argument = exp.getArgument();
		RosettaInterpreterValue interpretedArgument = argument.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(interpretedArgument)) {
			return interpretedArgument;
		}
		
		long count = RosettaInterpreterBaseValue.valueStream(interpretedArgument).count();
		
		boolean exists;
		switch (exp.getModifier()) {
			case SINGLE:
				exists = count == 1;
				break;
			case MULTIPLE:
				exists = count > 1;
				break;
			default:
				exists = count > 0;
		}
		
		return new RosettaInterpreterBooleanValue(exists);
	}

	
	/**
	 * Interprets an "is absent" expression.
	 * If the argument of the expression is of size 0, so:
	 * 	- either it is optional, (0..*), and it was not instantiated
	 *  - or it is a list with 0 elements []
	 *
	 * @param exp "Is absent" expression to intepret
	 * @return Boolean indicating if the interpreted argument is absent
	 */
	public RosettaInterpreterValue interp(RosettaAbsentExpression exp) {
		RosettaExpression argument = exp.getArgument();
		RosettaInterpreterValue interpretedArgument = argument.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(interpretedArgument)) {
			return interpretedArgument;
		}
		
		long count = RosettaInterpreterBaseValue.valueStream(interpretedArgument).count();
		boolean isAbsent = count == 0;
		return new RosettaInterpreterBooleanValue(isAbsent);
	}

	/**
	 * Interprets a count operation.
	 * Return the number of elements in a list
	 *
	 * @param exp Expression to perform 'count' on
	 * @return Integer indicating how many elements there are in the list
	 */
	public RosettaInterpreterValue interp(RosettaCountOperation exp) {
		RosettaExpression argument = exp.getArgument();
		RosettaInterpreterValue interpretedArgument = argument.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(interpretedArgument)) {
			return interpretedArgument;
		}
		
		long count = RosettaInterpreterBaseValue.valueStream(interpretedArgument).count();
		return new RosettaInterpreterIntegerValue(BigInteger.valueOf(count));
	}

	/**
	 * Interprets a first operation.
	 * If a list is not empty, it returns the first element.
	 * Otherwise, it returns an error.
	 *
	 * @param exp Expression on which to perform 'first' operation
	 * @return First element of the list
	 */
	public RosettaInterpreterValue interp(FirstOperation exp) {
		RosettaExpression argument = exp.getArgument();
		RosettaInterpreterValue interpretedArgument = argument.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(interpretedArgument)) {
			return interpretedArgument;
		}
		
		long count = RosettaInterpreterBaseValue.valueStream(interpretedArgument).count();
		if (count == 0L) {
			// List is empty
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("List is empty"));
		} else {
			// List has at least one element
			return RosettaInterpreterBaseValue.valueStream(interpretedArgument)
					.collect(Collectors.toList()).get(0);
		}
	}

	
	/**
	 * Interprets a last operation.
	 * If a list is not empty, it returns the last element.
	 * Otherwise, it returns an error.
	 *
	 * @param exp Expression on which to perform 'last' operation
	 * @return Last element of the list
	 */
	public RosettaInterpreterValue interp(LastOperation exp) {
		RosettaExpression argument = exp.getArgument();
		RosettaInterpreterValue interpretedArgument = argument.accept(visitor);
		
		if (RosettaInterpreterErrorValue.errorsExist(interpretedArgument)) {
			return interpretedArgument;
		}
		
		long count = RosettaInterpreterBaseValue.valueStream(interpretedArgument).count();
		if (count == 0L) {
			// List is empty
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("List is empty"));
		} else {
			// List has at least one element
			return RosettaInterpreterBaseValue.valueStream(interpretedArgument)
					.collect(Collectors.toList()).get((int)count - 1);
		}
	}

}
