package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
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
import com.regnosys.rosetta.rosetta.expression.impl.OneOfOperationImpl;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.impl.ConditionImpl;
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
			case "time": {
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
								"Constructor Expressions: time isn't valid.", expr));
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
								"Constructor Expressions: time isn't valid.", expr));
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
												+ "error value.",
												expr));
								
								return RosettaInterpreterErrorValue.merge(
										List.of(newExpError, expError));
							}
							contains = true;
							
							attributes.add(new 
								RosettaInterpreterTypedFeatureValue(name, value, card));
							env.addValue(name, value);
						}
					}
					if (!contains) {
						RosettaInterpreterListValue empty = 
								new RosettaInterpreterListValue(List.of());
						attributes.add(new 
								RosettaInterpreterTypedFeatureValue(name, empty, card));
						env.addValue(name, empty);
					}
				}
				
				//check conditions of type in separate method
				List<Condition> conditions = ((DataImpl) expr.getTypeCall().getType()).getConditions();
				
				String conditionsError = verifyConditions(conditions, attributes, env);
				if (conditionsError != null) {
					return new RosettaInterpreterErrorValue(
							new RosettaInterpreterError(conditionsError, expr));
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
				"Constructor Expressions: attribute type is not valid.", expr));
	}
	
	
	/**
	 * Check all the conditions of the type.
	 *
	 * @param conditions - conditions set by type
	 * @param attr - list of attributes of type
	 * @return Error message iff conditions not met, else null
	 */
	private String verifyConditions(List<Condition> conditions, List<RosettaInterpreterTypedFeatureValue> attr,
			RosettaInterpreterBaseEnvironment env) {
		for (Condition condInterface : conditions) {
			ConditionImpl c = (ConditionImpl)condInterface;
			if (c.getExpression().getClass().equals(ChoiceOperationImpl.class)) {
				ChoiceOperationImpl choice = (ChoiceOperationImpl)c.getExpression();
				List<String> choiceAttributesName = choice.getAttributes().stream()
						.map(Attribute::getName)
						.collect(Collectors.toList());
				
				return verifyChoiceOperation(choice.getNecessity(), choiceAttributesName, attr);
			}
			if (c.getExpression().getClass().equals(OneOfOperationImpl.class)) {
				List<String> allAttributesNames = attr.stream()
						.map(RosettaInterpreterTypedFeatureValue::getName)
						.collect(Collectors.toList());
				
				int nonEmptyCount = countPresentAttributes(allAttributesNames, attr);
				if (nonEmptyCount != 1) {
					return "One-of condition not followed. "
							+ "Exactly one attribute should be defined.";
				}
				
			} else {
				RosettaInterpreterValue result = c.getExpression().accept(visitor, env);
				
				if (!((RosettaInterpreterBooleanValue) result).getValue()) {
					return "Condition not followed.";
				}
			}
		}
		
		//if no errors up until this point, return null
		return null;
	}

	/**
	 * Verify the Choice operation of this type.
	 *
	 * @param necessity - Necessity of choice operation
	 * @param choiceAttributesName - the names of the attributes choice applies to
	 * @param attributes - all the attributes of object 
	 * @return Error message iff condition not met, else null
	 */
	private String verifyChoiceOperation(Necessity necessity, List<String> choiceAttributesName,
			List<RosettaInterpreterTypedFeatureValue> attributes) {
		if (necessity.equals(Necessity.REQUIRED)) {
			//exactly one attribute allowed to be present
			// => count non-empty, should be one
			int nonEmptyCount = countPresentAttributes(
					choiceAttributesName, 
					attributes);
			if (nonEmptyCount != 1) {
				return "Choice condition not followed. Exactly one attribute should be defined.";
			}
		}
		
		if (necessity.equals(Necessity.OPTIONAL)) {
			//at most one attribute allowed to be present
			// => count non-empty, should be less/equal than one
			int nonEmptyCount = countPresentAttributes(
					choiceAttributesName, 
					attributes);
			if (nonEmptyCount > 1) {
				return "Choice condition not followed. At most one attribute should be defined.";
			}
		}
		return null;
	}

	/**
	 * Counts how many attributes mentioned in the first list are non-empty.
	 *
	 * @param names - the names of the attributes that should be counted
	 * @param attributes - all the attributes of object
	 * @return the number of non-empty attributes
	 */
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
