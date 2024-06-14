package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.impl.DataImpl;

public class RosettaInterpreterRosettaConstructorExpressionInterpreter extends RosettaInterpreterConcreteInterpreter {

	@Inject
	RosettaExtensions ext = new RosettaExtensions();
	
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
				
				if (day instanceof RosettaInterpreterNumberValue 
						&& month instanceof RosettaInterpreterNumberValue 
						&& year instanceof RosettaInterpreterNumberValue) {
					return new RosettaInterpreterDateValue(
							((RosettaInterpreterNumberValue) day), 
							((RosettaInterpreterNumberValue) month),
							((RosettaInterpreterNumberValue) year));
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
				List<RosettaInterpreterTypedFeatureValue> attributes = new ArrayList<>();
				
				DataImpl data = (DataImpl) expr.getTypeCall().getType();
				// This retrieves all the attributes of a data type
				List<Attribute> allAtt = ext.allNonOverridesAttributes(data);
				
				for (Attribute att : allAtt) {
					String name = att.getName();
					RosettaCardinality card = att.getCard();
					boolean contains = false;
					
					// Here I get the attributes from the constructor
					for (ConstructorKeyValuePair pair : values) {
						// Check if the attributes from the data type are in
						// the list of the constructor attributes. If they are,
						// assign them a value, else they are empty. This is needed
						// for when '...' is used in the constructor.
						if (name.equals(pair.getKey().getName())) {
							RosettaInterpreterValue value = 
									pair.getValue().accept(visitor, env);
							
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
							contains = true;
							
							attributes.add(new 
								RosettaInterpreterTypedFeatureValue(name, value, card));
						}
					}
					if (!contains) {
						RosettaInterpreterListValue empty = 
								new RosettaInterpreterListValue(List.of());
						attributes.add(new 
								RosettaInterpreterTypedFeatureValue(name, empty, card));
					}
				}
				
				if (((DataImpl) expr.getTypeCall().getType()).hasSuperType()) {
					String superType = ((DataImpl) expr.getTypeCall().getType())
							.getSuperType().getName();
					
					return new RosettaInterpreterTypedValue(superType, typeCall, attributes);
				}
				
				return new RosettaInterpreterTypedValue(typeCall, attributes);
			}
		}
		
		return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
				"Constructor Expressions: attribute type is not valid."));
	}
}
