package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterIntegerValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterRosettaConstructorExpressionInterpreter extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interpreter method for Constructor Expressions.
	 *
	 * @param expr 		RosettaConstructorExpression to be interpreted
	 * @param env		the environment used
	 * @return 			the interpreted value
	 */
	public RosettaInterpreterBaseValue interp(
			RosettaConstructorExpression expr, RosettaInterpreterBaseEnvironment env) {
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
							((RosettaInterpreterIntegerValue) day), 
							((RosettaInterpreterIntegerValue) month),
							((RosettaInterpreterIntegerValue) year));
				}
				break;
			}
			case "dateTime": {
				RosettaInterpreterValue date = values.get(0).getValue().accept(visitor, env);
				RosettaInterpreterValue time = values.get(1).getValue().accept(visitor, env);
				
				if (date instanceof RosettaInterpreterDateValue 
						&& time instanceof RosettaInterpreterTimeValue) {
					boolean check = ((RosettaInterpreterTimeValue) time).valid();
					
					if (!check) {
						return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
								"Constructor Expressions: time isn't valid."));
					} else {
						return new RosettaInterpreterDateTimeValue(
							((RosettaInterpreterDateValue) date), 
							((RosettaInterpreterTimeValue) time));
					}
				}
				break;
			}
			case "zonedDateTime": {
				RosettaInterpreterValue date = values.get(0).getValue().accept(visitor, env);
				RosettaInterpreterValue time = values.get(1).getValue().accept(visitor, env);
				RosettaInterpreterValue timeZone = values.get(2).getValue().accept(visitor, env);
				
				if (date instanceof RosettaInterpreterDateValue 
						&& time instanceof RosettaInterpreterTimeValue 
						&& timeZone instanceof RosettaInterpreterStringValue) {
					boolean check = ((RosettaInterpreterTimeValue) time).valid();
					
					if (!check) {
						return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
								"Constructor Expressions: time isn't valid."));
					} else {
						return new RosettaInterpreterZonedDateTimeValue(
							((RosettaInterpreterDateValue) date), 
							((RosettaInterpreterTimeValue) time),
							((RosettaInterpreterStringValue) timeZone));
					}
				}
				break;
			}
			default: {
				// check that the data type is defined before, if not return error
				List<RosettaInterpreterTypedFeatureValue> attributes = new ArrayList<>();
				
				for (ConstructorKeyValuePair pair : values) {
					String name = pair.getKey().getName();
					RosettaInterpreterValue value = pair.getValue().accept(visitor, env);
					
					if (RosettaInterpreterErrorValue.errorsExist(value)) {
						RosettaInterpreterErrorValue expError = 
								(RosettaInterpreterErrorValue) value;
						RosettaInterpreterErrorValue newExpError = 
								new RosettaInterpreterErrorValue(
										new RosettaInterpreterError(
												"Constructor Expression"
												+ ": the attribute \""
												+ name + "\" is an "
												+ "error value."));
						
						return RosettaInterpreterErrorValue.merge(
								List.of(newExpError, expError));
					}
					
					attributes.add(new RosettaInterpreterTypedFeatureValue(name, value));
				}
				
				return new RosettaInterpreterTypedValue(typeCall, attributes);
			}
		}
		
		return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
				"Constructor Expressions: attribute type is not valid."));
	}
}
