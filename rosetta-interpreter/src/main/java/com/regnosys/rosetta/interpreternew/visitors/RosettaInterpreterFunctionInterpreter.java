package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterFunctionValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.impl.AttributeImpl;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;


public class RosettaInterpreterFunctionInterpreter
	extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interprets a function, returns the result.
	 *
	 * @param func The name of the function to interpret
	 * @param env RosettaInterpreterEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the function.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(FunctionImpl func, List<RosettaExpression> args,
			RosettaInterpreterEnvironment env) {
		
		final FunctionImpl f = ((RosettaInterpreterFunctionValue) env.findValue(func.getName()))
				.getFunction();
		RosettaInterpreterErrorValue acc = new RosettaInterpreterErrorValue();
		List<RosettaInterpreterValue> interpretedArgs = new ArrayList<>();
		RosettaInterpreterEnvironment nv = copyDataTypesOnly(env);
		
		acc = processInputs(f, args, env, interpretedArgs, acc, nv);
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		//check the pre-conditions of the function
		List<Condition> conditions = func.getConditions();
		acc = processConditions(conditions, acc, nv);
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		//Add all aliases to the environment, throw errors if any are found
		List<ShortcutDeclaration> aliases = f.getShortcuts();
		acc = processAliases(aliases, acc, nv);
		 if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		
		//compute the results of all operations in the function
		RosettaInterpreterValue result;
		if (nv.findValue(f.getOutput().getTypeCall().getType().getName())
				instanceof RosettaInterpreterTypedValue) {
			result = processOperationsTyped(f, acc, nv);
		} else {
			result = processOperations(f, acc, nv);
		}
		if (result instanceof RosettaInterpreterErrorValue) {
			return result;
		}
		
		//check the post-conditions of the function
		nv.addValue(f.getOutput().getName(), result);
		conditions = func.getPostConditions();
		acc = processConditions(conditions, acc, nv);
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		
		//if everything went well, return the result
		return result;
	}
	
	/**
	 * Subroutine for processing the inputs.
	 * 
	 *
	 * @param f The function we are interpreting
	 * @param args The raw arguments
	 * @param env The original environment
	 * @param interpretedArgs The interpreted arguments
	 * @param acc Error accumulator
	 * @param nv The new, local, environment
	 * @return The error accumulator, since all correct operations do not require a return value 
	 */
	public RosettaInterpreterErrorValue processInputs(FunctionImpl f, List<RosettaExpression> args,
			RosettaInterpreterBaseEnvironment env, List<RosettaInterpreterValue> interpretedArgs,
			RosettaInterpreterErrorValue acc, RosettaInterpreterBaseEnvironment nv) {

		//interpret the raw arguments
		for (RosettaExpression e : args) {
			interpretedArgs.add(e.accept(visitor, env));
		}
		
		//check if there are any errors in interpreting the arguments. If so, return them
		for (RosettaInterpreterValue value : interpretedArgs) {
				acc.addAllErrors(value);
		}
		if (acc.getErrors().size() > 0) {
			return acc;
		}
	
		//check that all argument/passed value correspond in type and cardinality
		//if not, return errors pointing to each attribute reference where this is the case
		int inputSize = f.getInputs().size();
		for (int i = 0 ; i < inputSize ; i++) {
			acc.addAllErrors(checkPair((AttributeImpl) f.getInputs().get(i), 
					(RosettaInterpreterBaseValue) interpretedArgs.get(i)));
		}
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		//create a copy of the enums and data types in the passed environment
		//since those are the only "global" variables
//				RosettaInterpreterEnvironment nv = copyDataTypesOnly(env);
		//add all the argument/value pairs to the NEW environment
		for (int i = 0 ; i < inputSize ; i++) {
			AttributeImpl attr = (AttributeImpl) f.getInputs().get(i);
			RosettaInterpreterBaseValue value = (RosettaInterpreterBaseValue) interpretedArgs.get(i);
			nv.addValue(attr.getName(), value);
		}
		return acc;
		
	}

	/**
	 * Subroutine for processing conditions.
	 * 
	 *
	 * @param conditions The list of conditions to check
	 * @param acc Error accumulator
	 * @param nv The new, local, environment
	 * @return The error accumulator, since all correct operations do not require a return value 
	 */
	public RosettaInterpreterErrorValue processConditions(List<Condition> conditions,
			RosettaInterpreterErrorValue acc, RosettaInterpreterBaseEnvironment nv) {

		for (Condition c : conditions) {
			RosettaInterpreterValue v = c.getExpression().accept(visitor, nv);
			if (v instanceof RosettaInterpreterBooleanValue) {
				if (((RosettaInterpreterBooleanValue) v).getValue() == false) {
					acc.addError(new RosettaInterpreterError("Condition \"" 
							+ c.getName() + "\" does not hold for this function call"));
				}
			} else { //must be an error if not a boolean value
				acc.addAllErrors(v);
			}
		}
		return acc;
	}
	
	/**
	 * Subroutine for processing aliases.
	 * 
	 *
	 * @param aliases The list of aliases to interpret
	 * @param acc Error accumulator
	 * @param nv The new, local, environment
	 * @return The error accumulator, since all correct operations do not require a return value 
	 */
	public RosettaInterpreterErrorValue processAliases(List<ShortcutDeclaration> aliases,
			RosettaInterpreterErrorValue acc, RosettaInterpreterBaseEnvironment nv) {

		for (ShortcutDeclaration alias : aliases) {
			RosettaInterpreterBaseValue val = (RosettaInterpreterBaseValue) 
					alias.getExpression().accept(visitor, nv);
			acc.addAllErrors(val);
			nv.addValue(alias.getName(), val);
		}
		return acc;
	}
	
	/**
	 * Subroutine for processing operations.
	 * 
	 *
	 * @param f The function we are interpreting
	 * @param acc Error accumulator
	 * @param nv The new, local, environment
	 * @return The error accumulator if there are any errors, and the result value otherwise
	 */
	public RosettaInterpreterValue processOperations(FunctionImpl f, 
			RosettaInterpreterErrorValue acc, RosettaInterpreterBaseEnvironment nv) {
		

		List<RosettaInterpreterValue> resultList = new ArrayList<>();;

		for (Operation o : f.getOperations()) {
			if (o.isAdd()) {
				
				resultList.add(o.getExpression().accept(visitor, nv));
			} else {
				resultList = ((RosettaInterpreterBaseValue) o.getExpression().accept(visitor, nv))
						.toValueStream().collect(Collectors.toList());
			}
		}
		
		//check that the function operations yield no errors
		for (RosettaInterpreterValue value : resultList) {
			acc.addAllErrors(value);
		} if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		//check cardinality and type of output matches computed value
		RosettaInterpreterBaseValue result;
		int upperLimit = f.getOutput().getCard().isUnbounded() ? Integer.MAX_VALUE : 
			f.getOutput().getCard().getSup();
		int lowerLimit = f.getOutput().getCard().getInf();
		//make the result a single element or a list depending on its stated cardinality
		if (upperLimit == 1 && lowerLimit == 1) {
			result = (RosettaInterpreterBaseValue) resultList.get(0);
		} else {
			result = new RosettaInterpreterListValue(resultList);
		}
		acc.addAllErrors(checkPair((AttributeImpl) f.getOutput(), result));
		if (acc.getErrors().size() > 0) {
			return acc; 
		} else {
			return result;
		}
	}
	
	/**
	 * Subroutine for processing operations when the output is a custom typed value.
	 * 
	 *
	 * @param f The function we are interpreting
	 * @param acc Error accumulator
	 * @param nv The new, local, environment
	 * @return The error accumulator if there are any errors, and the result value otherwise
	 */
	public RosettaInterpreterValue processOperationsTyped(FunctionImpl f,
			RosettaInterpreterErrorValue acc, RosettaInterpreterBaseEnvironment nv) {
		

		RosettaInterpreterTypedValue result = (RosettaInterpreterTypedValue)
				nv.findValue(f.getOutput().getTypeCall().getType().getName());

		for (Operation o : f.getOperations()) {
			if (o.getPath() != null) {
				String featureName = o.getPath().getAttribute().getName();
				RosettaInterpreterValue value = o.getExpression().accept(visitor, nv);
				acc.addAllErrors(value);
				for (RosettaInterpreterTypedFeatureValue v : result.getAttributes()) {
					if (v.getName().equals(featureName)) {
						v.setValue(value);
					}
				}
			} else {
				result = (RosettaInterpreterTypedValue) o.getExpression().accept(visitor, nv);
			}
		}
		
		//check that the function operations yield no errors
		if (acc.getErrors().size() > 0) {
			return acc;
		} else {
			return result;
		}
	}
	
	/**
	 * Subroutine for checking that the function call is valid based on cardinality and type checking.
	 *
	 * @param attr The attribute object that contains the pertinent information
	 * @param value The interpreted value that is passed to the function
	 * @return True if the type and cardinality of the attribute and value match, false otherwise
	 */
	public RosettaInterpreterErrorValue checkPair(AttributeImpl attr, RosettaInterpreterBaseValue value) {
		//This will consider only basic types, this will be changed after datatypes are done
		String typeName = attr.getTypeCall().getType().getName();
		List<RosettaInterpreterValue> vals = value.toValueStream()
				.collect(Collectors.toList());
		
		//check that the cardinality of the arg and the value match
		int paramSize = vals.size();
		int lowerLimit = attr.getCard().getInf();
		if (paramSize < lowerLimit) {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" has cardinality lower than the limit "
							+ lowerLimit));
		}
		int upperLimit = attr.getCard().isUnbounded() ? Integer.MAX_VALUE : attr.getCard().getSup();
		if (paramSize > upperLimit) {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" has cardinality higher than the limit "
							+ upperLimit));
		}
		
		//checking that the potential list of elements in arg and value are of the same type
		switch (typeName) {
			case "number": 
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterNumberValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a number, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "int": 
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterNumberValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a number, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "boolean": 
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterBooleanValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a boolean, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "string": 
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterStringValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a string, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "date":
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterDateValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a date, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "dateTime":
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterDateTimeValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
							+ attr.getName() + "\" requires a dateTime, but received a "
								+ val.getClass()));
					}
				}
				break;
			case "zonedDateTime":
				for (RosettaInterpreterValue val : vals) {
					if (!(val instanceof RosettaInterpreterZonedDateTimeValue)) {
						return new RosettaInterpreterErrorValue(
								new RosettaInterpreterError("The attribute \"" 
						+ attr.getName() + "\" requires a zonedDateTime, but received a "
								+ val.getClass()));
					}
				}
				break;
			default:
		}
		//if all checks pass, return true
		return new RosettaInterpreterErrorValue();

	}
	
	/**
	 * Subroutine for copying data type/enum declarations from the old environment.
	 *
	 * @param env Old environment
	 * @return new environment that contains the filtered values from the old one
	 */
	public RosettaInterpreterEnvironment copyDataTypesOnly(RosettaInterpreterEnvironment env) {
		RosettaInterpreterEnvironment result = new RosettaInterpreterEnvironment();
		Map<String, RosettaInterpreterValue> environment = env.getEnvironment();
		for (Map.Entry<String, RosettaInterpreterValue> entry : environment.entrySet()) {
			if (entry.getValue() instanceof RosettaInterpreterEnumValue
				|| entry.getValue() instanceof RosettaInterpreterTypedValue) {
				result.addValue(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
		
}
