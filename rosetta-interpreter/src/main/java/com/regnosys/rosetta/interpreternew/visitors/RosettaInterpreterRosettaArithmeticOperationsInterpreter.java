package com.regnosys.rosetta.interpreternew.visitors;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.rosetta.model.lib.RosettaNumber;

public class RosettaInterpreterRosettaArithmeticOperationsInterpreter 
					extends RosettaInterpreterConcreteInterpreter {
	
	
	/**
	 * Interprets an arithmetic operation, evaluating the operation between the two terms.
	 *
	 * @param expr The ArithmeticOperation expression to interpret
	 * @return If no errors are encountered, a RosettaInterpreterNumberValue or
	 * 		   RosettaInterpreterStringValue representing
	 * 		   the result of the arithmetic/concatenation operation.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(ArithmeticOperation expr,
			RosettaInterpreterEnvironment env) {

		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();
		RosettaInterpreterValue leftInterpreted = left.accept(visitor, env);
		RosettaInterpreterValue rightInterpreted = right.accept(visitor, env); 
		
		//Check that the types are correct for the operations
		if (!(leftInterpreted instanceof RosettaInterpreterNumberValue 
				|| leftInterpreted instanceof RosettaInterpreterStringValue 
				|| leftInterpreted instanceof RosettaInterpreterDateValue) 
				|| !(rightInterpreted instanceof RosettaInterpreterNumberValue
				|| rightInterpreted instanceof RosettaInterpreterStringValue 
				|| rightInterpreted instanceof RosettaInterpreterDateValue)) {
			
			// Check for errors in the left or right side of the binary operation
			RosettaInterpreterErrorValue leftErrors = 
					checkForErrors(leftInterpreted, "Leftside");
			RosettaInterpreterErrorValue rightErrors = 
					checkForErrors(rightInterpreted, "Rightside");
			return RosettaInterpreterErrorValue.merge(List.of(leftErrors, rightErrors));
		}	
		
		//Interpret string concatenation
		if (leftInterpreted instanceof RosettaInterpreterStringValue
				&& rightInterpreted instanceof RosettaInterpreterStringValue) {
			String leftString = ((RosettaInterpreterStringValue) leftInterpreted)
					.getValue();
			String rightString = ((RosettaInterpreterStringValue) rightInterpreted)
					.getValue();
			if (expr.getOperator().equals("+")) {
				return new RosettaInterpreterStringValue(leftString + rightString);
				}
			else {
				return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
				"Both terms are strings but the operation "
				+ "is not concatenation: not implemented"));
			}
		//Interpret number operations
		} else if (leftInterpreted instanceof RosettaInterpreterNumberValue
				&& rightInterpreted instanceof RosettaInterpreterNumberValue) {
			RosettaNumber leftNumber = ((RosettaInterpreterNumberValue) leftInterpreted).getValue();
			RosettaNumber rightNumber = ((RosettaInterpreterNumberValue) rightInterpreted).getValue();
			
			if (expr.getOperator().equals("+")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.add(rightNumber)).bigDecimalValue());
			} else if (expr.getOperator().equals("-")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.subtract(rightNumber)).bigDecimalValue());
			} else if (expr.getOperator().equals("*")) {
				return new RosettaInterpreterNumberValue((leftNumber
						.multiply(rightNumber)).bigDecimalValue());
			} else {
				// Division by 0 is not allowed
				if (rightNumber.floatValue() == 0.0) {
					return new RosettaInterpreterErrorValue(
							new RosettaInterpreterError(
							"Division by 0 is not allowed"));
				}
				return new RosettaInterpreterNumberValue((leftNumber
						.divide(rightNumber)).bigDecimalValue());
			}
		} else if (leftInterpreted instanceof RosettaInterpreterDateValue
				&& rightInterpreted instanceof RosettaInterpreterDateValue) {
			RosettaInterpreterDateValue l = (RosettaInterpreterDateValue) leftInterpreted;
			RosettaInterpreterDateValue r = (RosettaInterpreterDateValue) rightInterpreted;
			if (expr.getOperator().equals("-")) {
				String dayL = l.getDay().getValue().bigDecimalValue().toBigInteger().toString(); 
				if (dayL.length() == 1) {
					dayL = "0" + dayL;
				}
				String monthL = l.getMonth().getValue().bigDecimalValue().toBigInteger().toString(); 
				if (monthL.length() == 1) {
					monthL = "0" + monthL;
				}
				String yearL = l.getYear().getValue().bigDecimalValue().toBigInteger().toString(); 
				String dayR = r.getDay().getValue().bigDecimalValue().toBigInteger().toString(); 
				if (dayR.length() == 1) {
					dayR = "0" + dayR;
				}
				String monthR = r.getMonth().getValue().bigDecimalValue().toBigInteger().toString(); 
				if (monthR.length() == 1) {
					monthR = "0" + monthR;
				}
				String yearR = r.getYear().getValue().bigDecimalValue().toBigInteger().toString(); 
				
				String inputString1 = dayL + " " + monthL + " " + yearL; 
				String inputString2 = dayR + " " + monthR + " " + yearR; 

				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy");

			    LocalDateTime date1 = LocalDate.parse(inputString1, dtf).atStartOfDay();
			    LocalDateTime date2 = LocalDate.parse(inputString2, dtf).atStartOfDay();
			    long daysBetween = Duration.between(date1, date2).toDays();
			    return new RosettaInterpreterNumberValue(BigDecimal.valueOf(daysBetween));
				} else {
					return new RosettaInterpreterErrorValue(
							new RosettaInterpreterError(
						"Both terms are dates but the operation "
						+ "is not subtraction: not implemented"));
					}
		} else {
			return new RosettaInterpreterErrorValue(
				new RosettaInterpreterError(
				"The terms of the operation are not both strings or both numbers or both dates"));
		}	
	}
	
	
	/**
	 * Helper method that takes an interpretedValue and a string,
	 * and returns the correct error which
	 * interpretedValue causes, if any.
	 *
	 * @param interpretedValue The interpreted value which we check for errors
	 * @param side String containing either "Leftside" or "Rightside", 
	 *        purely for clearer error messages
	 * @return The correct RosettaInterpreterErrorValue, or "null" 
	 *         if the interpretedValue does not cause an error
	 */
	private RosettaInterpreterErrorValue checkForErrors(
			RosettaInterpreterValue interpretedValue, String side) {
		if  (interpretedValue instanceof RosettaInterpreterNumberValue 
				|| interpretedValue instanceof RosettaInterpreterStringValue
				|| interpretedValue instanceof RosettaInterpreterDateValue) {
			// If the value satisfies the type conditions, we return an empty 
			// error value so that the merger has two error values to merge
			return new RosettaInterpreterErrorValue();
		}
		
		else if (RosettaInterpreterErrorValue.errorsExist(interpretedValue)) {
			// The interpreted value was an error so we propagate it
			return (RosettaInterpreterErrorValue) interpretedValue;
		} else {
			// The interpreted value was not an error,
			// but something other than a string or number
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							"Arithmetic Operation: " + side 
							+ " is not of type Number/String/Date"));
		}
	}
}

