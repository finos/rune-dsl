package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;

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
import com.regnosys.rosetta.rosetta.expression.Necessity;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ChoiceOperationImpl;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.impl.AttributeImpl;
import com.regnosys.rosetta.rosetta.simple.impl.ConditionImpl;
import com.regnosys.rosetta.rosetta.simple.impl.DataImpl;

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
				
				// This for will take all the attributes of a data type, 
				// including the ones of the supertype
				for (ConstructorKeyValuePair pair : values) {
					String name = pair.getKey().getName();
					RosettaCardinality card = ((AttributeImpl) pair.getKey()).getCard();
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
					
					attributes.add(new RosettaInterpreterTypedFeatureValue(name, value, card));
				}
				
				//check if attributes are correctly defined, considering the conditions
				List<Condition> conditions = ((DataImpl) expr.getTypeCall().getType()).getConditions();
				
				for (Condition condInterface : conditions) {
					ConditionImpl c = (ConditionImpl)condInterface;
					if (c.getExpression().getClass().equals(ChoiceOperationImpl.class)) {
						ChoiceOperationImpl choice = (ChoiceOperationImpl)c.getExpression();
						List<String> choiceAttributesName = choice.getAttributes().stream()
								.map(Attribute::getName)
								.collect(Collectors.toList());
						
						if (choice.getNecessity().equals(Necessity.REQUIRED)) {
							//exactly one attribute allowed to be present
							// => count non-empty, should be one
							int nonEmptyCount = countPresentAttributes(
									choiceAttributesName, 
									attributes);
							if (nonEmptyCount != 1) {
								return new RosettaInterpreterErrorValue(
										new RosettaInterpreterError(
										"Choice condition not followed. "
										+ "Exactly one attribute should "
										+ "be defined."));
							}
						}
						
						if (choice.getNecessity().equals(Necessity.OPTIONAL)) {
							//at most one attribute allowed to be present
							// => count non-empty, should be less/equal than one
							int nonEmptyCount = countPresentAttributes(
									choiceAttributesName, 
									attributes);
							if (nonEmptyCount > 1) {
								return new RosettaInterpreterErrorValue(
										new RosettaInterpreterError(
										"Choice condition not followed. "
										+ "At most one attribute should "
										+ "be defined."));
							}
						}
						
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

	private int countPresentAttributes(List<String> names, List<RosettaInterpreterTypedFeatureValue> attributes) {
		int countEmpty = 0;
		

		Map<String, RosettaInterpreterValue> valueMap = attributes.stream()
				.collect(Collectors.toMap(
						RosettaInterpreterTypedFeatureValue::getName,
						RosettaInterpreterTypedFeatureValue::getValue));
		
		for (String name : names) {
			RosettaInterpreterValue val = valueMap.get(name);
			//attribute empty if it is ListValue with size 0
			if (val instanceof RosettaInterpreterListValue) {
				if (((RosettaInterpreterListValue)val).getExpressions().size() == 0) {
					countEmpty++;
				}
			}
		}
		
		return attributes.size() - countEmpty;
	}
}
