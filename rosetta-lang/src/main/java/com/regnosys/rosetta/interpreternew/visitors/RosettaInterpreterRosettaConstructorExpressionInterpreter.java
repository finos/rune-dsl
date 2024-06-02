package com.regnosys.rosetta.interpreternew.visitors;

import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConstructorExpressionInterpreter extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterBaseValue interp
	(RosettaConstructorExpression expr, RosettaInterpreterBaseEnvironment env) {
		String typeCall = expr.getTypeCall().getType().getName();
		EList<ConstructorKeyValuePair> values = expr.getValues();
		
		switch (typeCall) {
			case "date": {
				RosettaInterpreterValue day = values.get(0).getValue().accept(visitor, env);
				RosettaInterpreterValue month = values.get(1).getValue().accept(visitor, env);
				RosettaInterpreterValue year = values.get(2).getValue().accept(visitor, env);
				
				if (day instanceof RosettaInterpreterIntegerValue 
						&& month instanceof RosettaInterpreterIntegerValue 
						&& year instanceof RosettaInterpreterIntegerValue) {
					return new RosettaInterpreterDateValue(
							((RosettaInterpreterIntegerValue) day).getValue().intValue(), 
							((RosettaInterpreterIntegerValue) month).getValue().intValue(),
							((RosettaInterpreterIntegerValue) year).getValue().intValue());
				}
				break;
			}
			case "dateTime": {
				RosettaInterpreterValue date = values.get(0).getValue().accept(visitor, env);
				RosettaInterpreterValue time = values.get(1).getValue().accept(visitor, env);
				
				if (date instanceof RosettaInterpreterDateValue 
						&& time instanceof RosettaInterpreterTimeValue) {
					return new RosettaInterpreterDateTimeValue(
							((RosettaInterpreterDateValue) date), 
							((RosettaInterpreterTimeValue) time));
				}
				break;
			}
			case "zonedDateTime": {
				RosettaInterpreterValue date = values.get(0).getValue().accept(visitor, env);
				RosettaInterpreterValue time = values.get(1).getValue().accept(visitor, env);
				RosettaInterpreterValue timeZone = values.get(2).getValue().accept(visitor, env);
				
				if (date instanceof RosettaInterpreterIntegerValue 
						&& time instanceof RosettaInterpreterIntegerValue 
						&& timeZone instanceof RosettaInterpreterIntegerValue) {
					return new RosettaInterpreterZonedDateTimeValue(
							((RosettaInterpreterDateValue) date), 
							((RosettaInterpreterTimeValue) time),
							((RosettaInterpreterStringValue) timeZone).getValue());
				}
				break;
			}
			default: {
				// needed for data types
				break;
			}
		}
		
		return null;
	}
}
