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
			switch (feature) {
			case "day": return ((RosettaInterpreterDateValue) receiverValue).getDay();
			case "month": return ((RosettaInterpreterDateValue) receiverValue).getMonth();
			case "year": return ((RosettaInterpreterDateValue) receiverValue).getYear();
			default: //error
			}
			
		} else if (receiverValue instanceof RosettaInterpreterDateTimeValue) {
			switch (feature) {
			case "date": return ((RosettaInterpreterDateTimeValue) receiverValue).getDate();
			case "time": return ((RosettaInterpreterDateTimeValue) receiverValue).getTime();
			default: //error
			}
			
		} else if (receiverValue instanceof RosettaInterpreterZonedDateTimeValue) {
			switch (feature) {
			case "date": return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getDate();
			case "time": return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getTime();
			case "timezone": return ((RosettaInterpreterZonedDateTimeValue) receiverValue).getTimeZone();
			default: //error
			}
			
		} else {
			// add implementation for data types and error handling
		}
		return null;
	}
}
