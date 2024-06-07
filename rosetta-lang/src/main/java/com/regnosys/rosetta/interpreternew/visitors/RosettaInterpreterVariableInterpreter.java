package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitor;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterFunctionValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.impl.AttributeImpl;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;

public class RosettaInterpreterVariableInterpreter {
	
	@Inject
	protected RosettaInterpreterVisitor visitor = new RosettaInterpreterVisitor();
	
	
	
	/**
	 * Interprets a variable, returns the value of it.
	 *
	 * @param exp The RosettaSymbolReference expression to interpret
	 * @param env RosettaInterpreterBaseEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the variable.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(RosettaSymbolReference exp, 
			RosettaInterpreterEnvironment env) {
		
		if (exp.getSymbol() instanceof FunctionImpl) {
			return interp((FunctionImpl) exp.getSymbol(), exp.getRawArgs(), env);
		}
		
		return interp(exp.getSymbol().getName(), env);
	}
		
	/**
	 * Interprets a variable, returns the value of it.
	 *
	 * @param varName The name of the variable to interpret
	 * @param env RosettaInterpreterEnvironment that keeps track
	 *		   of the current state of the program
	 * @return If no errors are encountered, a RosettaInterpreterValue representing
	 * 		   the value of the variable.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(String varName, 
			RosettaInterpreterEnvironment env) {
		
		//Search for variable in environment
		RosettaInterpreterBaseValue varValue = env.findValue(varName);
		
		return varValue;
	}
	
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
	
		//interpret the raw arguments
		List<RosettaInterpreterValue> interpretedArgs = new ArrayList<>();
		for (RosettaExpression e : args) {
			interpretedArgs.add(e.accept(visitor, env));
		}
		
		//check if there are any errors in interpreting the arguments. If so, return them
		RosettaInterpreterErrorValue acc = new RosettaInterpreterErrorValue();
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
		
		//add all the argument/value pairs to the NEW environment
		RosettaInterpreterEnvironment nv = new RosettaInterpreterEnvironment();
		for (int i = 0 ; i < inputSize ; i++) {
			AttributeImpl attr = (AttributeImpl) f.getInputs().get(i);
			RosettaInterpreterBaseValue value = (RosettaInterpreterBaseValue) interpretedArgs.get(i);
			nv.addValue(attr.getName(), value);
		}
		
		//check the pre-conditions of the function
		List<Condition> conditions = func.getConditions();
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
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		//Add all aliases to the environment, throw errors if any are found
		List<ShortcutDeclaration> aliases = f.getShortcuts();
		for (ShortcutDeclaration alias : aliases) {
			RosettaInterpreterBaseValue val = (RosettaInterpreterBaseValue) 
					alias.getExpression().accept(visitor, nv);
			acc.addAllErrors(val);
			nv.addValue(alias.getName(), val);
		} if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		
		//compute the results of all operations in the function
		List<RosettaInterpreterValue> resultList = new ArrayList<>();
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
		}
		
		//check the post-conditions of the function
		nv.addValue(f.getOutput().getName(), result);
		conditions = func.getPostConditions();
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
		if (acc.getErrors().size() > 0) {
			return acc;
		}
		
		
		//if everything went well, return the result
		return result;
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
	//			this will be implemented after record types
	//			case "time":
	//				if (!(val instanceof RosettaInterpreterNumberValue
	//						|| val instanceof RosettaInterpreterIntegerValue)) {
	//					return false;
	//				}
			default:
		}
		//if all checks pass, return true
		return new RosettaInterpreterErrorValue();

	}

}
