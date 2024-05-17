package com.regnosys.rosetta.interpreternew.visitors;

import java.util.Arrays;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEqualityOperationInterpreter extends 
	RosettaInterpreterConcreteInterpreter {
	
	private static List<String> equalityOperators = Arrays.asList("=", "<>");
	
	/**
	 * Interprets an equality operation by evaluating both sides of the expression and
	 * determining if they are equal.
	 *
	 * @param expr equality operation expression to interpret
	 * @return If no errors are encountered, a RosettaInterpreterBooleanValue representing
	 * 		   the result of the equality 
	 *         comparison.
     *         It will be true if both evaluated expressions are equal, otherwise false.
     *         If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterBaseValue interp(EqualityOperation expr) {
		if (!equalityOperators.contains(expr.getOperator())) {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"operator not suppported")); 
		}
		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();
		
		RosettaInterpreterValue leftValue = left.accept(visitor);
		RosettaInterpreterValue rightValue = right.accept(visitor);
		
		boolean comparisonResult = leftValue.equals(rightValue);
		
		switch (expr.getCardMod()) {
		case NONE:
			//normally compare left and right side.
			boolean result = compareComparableValues(comparisonResult,
					expr.getOperator());
			return new RosettaInterpreterBooleanValue(result);
		
		case ANY:
			return compareAny(leftValue, rightValue, expr.getOperator());
			
		case ALL:
			return compareAll(leftValue, rightValue, expr.getOperator());

		default:
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"cardinality modifier " + expr.getCardMod()
							+ " not supported"));
			
		}		
	}
	
	private RosettaInterpreterBaseValue compareAny(RosettaInterpreterValue leftValue, 
			RosettaInterpreterValue rightValue, 
			String operator) {
		//list vs list case:
		if (leftValue instanceof RosettaInterpreterListValue 
				&& rightValue instanceof RosettaInterpreterListValue) {
			
			//only way this is allowed is if rightValue has a length of 1
			// and left has length more than 1
			RosettaInterpreterListValue rgtList =
					(RosettaInterpreterListValue) rightValue;
			RosettaInterpreterListValue lfList = 
					(RosettaInterpreterListValue) leftValue;
			if (rgtList.getExpressions().size() == 1
					&& lfList.getExpressions().size() > 1) {
				
				
				//for all elements in left list, check if the comparison 
				// between them and right-hand side is true
				boolean anyTrue = true;
				for (RosettaInterpreterValue e : lfList.getExpressions()) {
					boolean comparisonResult = 
							e.equals(rgtList.getExpressions().get(0));
					anyTrue |= compareComparableValues(comparisonResult, 
							operator);
				}
				return new RosettaInterpreterBooleanValue(anyTrue);
			}
			else {
				return new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(
								"cannot compare two lists"));
			}
		}
		
		//list vs element case:
		else if (leftValue instanceof RosettaInterpreterListValue) {
			
			RosettaInterpreterListValue lfList = 
					(RosettaInterpreterListValue) leftValue;
			
			//only way this is allowed is if left side has a length of
			//  more than 1
			if (lfList.getExpressions().size() > 1) {
			
				//for all elements in left list, check if the comparison 
				// between them and right-hand side is true
				boolean anyTrue = false;
				for (RosettaInterpreterValue e : lfList.getExpressions()) {
					boolean comparisonResult = e.equals(rightValue);
					anyTrue |= compareComparableValues(comparisonResult, 
							operator);
				}
				return new RosettaInterpreterBooleanValue(anyTrue); 
			}
		}
		else {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"cannot use \"ANY\" keyword "
							+ "to compare two elements"));
		}
		return new RosettaInterpreterBooleanValue(false);
	}
	
	private RosettaInterpreterBaseValue compareAll(RosettaInterpreterValue leftValue, 
			RosettaInterpreterValue rightValue, 
			String operator) {
		//list vs list case:
		if (leftValue instanceof RosettaInterpreterListValue 
				&& rightValue instanceof RosettaInterpreterListValue) {
			
			//only way this is allowed is if rightValue has a length of 1
			// and left has length more than 1
			RosettaInterpreterListValue rgtList =
					(RosettaInterpreterListValue) rightValue;
			RosettaInterpreterListValue lfList = 
					(RosettaInterpreterListValue) leftValue;
			if (rgtList.getExpressions().size() == 1
					&& lfList.getExpressions().size() > 1) {
				
				
				//for all elements in left list, check if the comparison 
				// between them and right-hand side is true
				boolean allTrue = true;
				for (RosettaInterpreterValue e : lfList.getExpressions()) {
					boolean comparisonResult = 
							e.equals(rgtList.getExpressions().get(0));
					allTrue &= compareComparableValues(comparisonResult, 
							operator);
				}
				return new RosettaInterpreterBooleanValue(allTrue);
			}
			else {
				//TODO: throw exception, cannot compare two lists.
				return new RosettaInterpreterErrorValue(
						new RosettaInterpreterError(
								"cannot compare two lists"));
			}
		}
		
		//list vs element case:
		else if (leftValue instanceof RosettaInterpreterListValue) {
			
			RosettaInterpreterListValue lfList = 
					(RosettaInterpreterListValue) leftValue;
			
			//only way this is allowed is if left side has a length of
			//  more than 1
			if (lfList.getExpressions().size() > 1) {
			
				//for all elements in left list, check if the comparison 
				// between them and right-hand side is true
				boolean allTrue = true;
				for (RosettaInterpreterValue e : lfList.getExpressions()) {
					boolean comparisonResult = e.equals(rightValue);
					allTrue &= compareComparableValues(comparisonResult,
							operator);
				}
				return new RosettaInterpreterBooleanValue(allTrue); 
			}
		}
		else {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"cannot use \"ALL\" keyword "
							+ "to compare two elements"));
			
		}
		return new RosettaInterpreterBooleanValue(false);
	}
	
	private boolean compareComparableValues(boolean comparisonResult, String operator) {
		if (operator.equals("=")) {
			return comparisonResult;
		}
		else {
			return !comparisonResult ;
		}
	}

}
