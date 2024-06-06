package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaFeatureCallInterpreter extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interpreter method for Feature Calls.
	 *
	 * @param exp 		RosettaFeatureCall to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(RosettaFeatureCall exp, RosettaInterpreterBaseEnvironment env) {
		RosettaExpression receiver = exp.getReceiver();
		RosettaInterpreterValue receiverValue = receiver.accept(visitor, env);
		
		String feature = exp.getFeature().getGetNameOrDefault();
		
		if (receiverValue instanceof RosettaInterpreterDateValue) {
			if (feature.equals("day")) {
				return ((RosettaInterpreterDateValue) receiverValue).getDay();
			} else if (feature.equals("month")) {
				return ((RosettaInterpreterDateValue) receiverValue).getMonth();
			} else {
				return ((RosettaInterpreterDateValue) receiverValue).getYear();
			}
			
		} else if (receiverValue instanceof RosettaInterpreterDateTimeValue) {
			if (feature.equals("date")) {
				return ((RosettaInterpreterDateTimeValue) receiverValue).getDate();
			} else {
				return ((RosettaInterpreterDateTimeValue) receiverValue).getTime();
			}
			
		} else if (receiverValue instanceof RosettaInterpreterZonedDateTimeValue) {
			if (feature.equals("date")) {
				return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getDate();
			} else if (feature.equals("time")) {
				return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getTime();
			} else {
				return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getTimeZone();
			}
			
		} else {
			// add implementation for data types and error handling
		}
		return null;
	}
}
