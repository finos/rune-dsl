package com.regnosys.rosetta.interpreternew.visitors;

import java.util.*;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;

public class RosettaInterpreterOnlyExistsInterpreter extends RosettaInterpreterConcreteInterpreter {
	
	/**
     * Interprets a 'only exists' expression.
     *
     * @param exp the 'only exists' expression to interpret.
     * @param env the environment containing variable bindings.
     * @return a boolean value indicating if only the specified features exist.
     */
    public RosettaInterpreterValue interp(RosettaOnlyExistsExpression exp, RosettaInterpreterBaseEnvironment env) {
        Set<String> expectedFeatures = new HashSet<>();

        for (RosettaExpression expression : exp.getArgs()) {
            RosettaFeatureCall featureCall = (RosettaFeatureCall) expression;
            
            // Recursively get the receiver and get the final feature value
            RosettaInterpreterValue finalAttribute = getAttributeUtil(featureCall, env);
            if (finalAttribute == null) {
            	return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
            			"The final attribute was not found"));
            } else if (finalAttribute instanceof RosettaInterpreterErrorValue) {
                return finalAttribute;
            } else if (!(finalAttribute instanceof RosettaInterpreterTypedFeatureValue)) {
            	return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
            			"The final attribute is not a feature"));
            }
            
            // Add the feature name to the set of expected features
            expectedFeatures.add(((RosettaInterpreterTypedFeatureValue)finalAttribute).getName());
        }

        // Get the variable (from the environment) that we need to check the attributes for
        RosettaExpression firstExpression = exp.getArgs().get(0);
        RosettaFeatureCall firstFeatureCall = (RosettaFeatureCall) firstExpression;
        
        RosettaInterpreterValue objectInstance = getReceiverUtil(firstFeatureCall.getReceiver(), env);
        // Error handling
        objectInstance = validateReceiver(objectInstance);
        if (objectInstance instanceof RosettaInterpreterErrorValue) {
            return objectInstance;
        }
        
        // Check if the only non-null attributes are the expected features.
        // We expect either to find an attribute from expectedFeatures that is declared
        // or find one that is not in expectedFeatures and is not declared
        List<RosettaInterpreterTypedFeatureValue> attributes = 
        		((RosettaInterpreterTypedValue) objectInstance).getAttributes();
        boolean onlyExpectedFeaturesExist = attributes.stream()
            .allMatch(attr -> expectedFeatures.contains(attr.getName()) == isDeclared(attr));

        return new RosettaInterpreterBooleanValue(onlyExpectedFeaturesExist);
    }
    
    // Checks if an attribute is declared
    private boolean isDeclared(RosettaInterpreterTypedFeatureValue attr) {
    	// Returns true if attribute is not a RosettaInterpreterListValue 
    	// containing an empty list of expressions
    	if (attr.getValue() instanceof RosettaInterpreterListValue) {
    		return !((RosettaInterpreterListValue) attr.getValue()).getExpressions().isEmpty();
    	}
    		
    	return true; // if it's not a RosettaInterpreterListValue it cannot possibly be "empty"
    }

    
    // This is just used to get the final attribute's name from a feature call.
    //
    // Example:
   	// For "(foo -> bar -> firstNumber, foo -> bar -> secondNumber) only exists" it will 
    // get "firstNumber" from the first element, and "secondNumber" from the second one.
    // These will then be saved in the "expectedFeatures" set so we know which ones are 
    // supposed to be declared in the "bar" object.
    private RosettaInterpreterValue getAttributeUtil(RosettaFeatureCall featureCall, 
    		RosettaInterpreterBaseEnvironment env) {
        // Recursively get the next receiver until the last one
    	// (foo -> bar -> baz -> attribute) would mean that the last receiver is "baz"
    	RosettaInterpreterValue receiver = getReceiverUtil(featureCall.getReceiver(), env);
        // Error handling
    	receiver = validateReceiver(receiver);
        if (receiver instanceof RosettaInterpreterErrorValue) {
            return receiver;
        }
        
        // Find the correct attribute in the receiver's attribute list
        String featureName = featureCall.getFeature().getName();
        return ((RosettaInterpreterTypedValue)receiver).getAttributes().stream()
            .filter(attr -> attr.getName().equals(featureName))
            .findFirst()
            .orElse(null);
    }

    
    // Recursively gets the receiver value of a feature call.
    //
    // Example:
    // For "foo -> bar -> baz -> attr only exists", it will recursively get the rightside
    // of the "->", until it gets to the last receiver, which is "baz" in this case.
    // "baz" will be used afterwards to check which attributes it contains that are/are not declared.
    private RosettaInterpreterValue getReceiverUtil(RosettaExpression receiver, RosettaInterpreterBaseEnvironment env) {
        if (receiver instanceof RosettaSymbolReference) {
        	// Meaning we already got to the end, we don't have another feature call
            RosettaSymbolReference ref = (RosettaSymbolReference) receiver;
            String receiverSymbolName = ref.getSymbol().getName();
            return (RosettaInterpreterTypedValue) env.findValue(receiverSymbolName);
            
        } else if (receiver instanceof RosettaFeatureCall) {
        	// We need to recursively get the next receiver
            RosettaFeatureCall featureCall = (RosettaFeatureCall) receiver;           
            RosettaInterpreterValue nextReceiver = getReceiverUtil(featureCall.getReceiver(), env);
            // Error handling
            nextReceiver = validateReceiver(nextReceiver);
            if (nextReceiver instanceof RosettaInterpreterErrorValue) {
                return nextReceiver;
            }
            
            String featureName = featureCall.getFeature().getName();
            return ((RosettaInterpreterTypedValue) nextReceiver).getAttributes().stream()
                .filter(attr -> attr.getName().equals(featureName))
                .map(RosettaInterpreterTypedFeatureValue::getValue)
                .filter(value -> value instanceof RosettaInterpreterTypedValue)
                .map(value -> (RosettaInterpreterTypedValue) value)
                .findFirst().orElse(null);
        }
        return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
    			"Receiver is not of correct type: only 'feature call'/'symbol reference' are accepted"));
    }
    
    private RosettaInterpreterValue validateReceiver(RosettaInterpreterValue receiver) {
    	// Just error handling
        if (receiver == null) {
            return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
                    "Receiver was not found"));
        } else if (receiver instanceof RosettaInterpreterErrorValue) {
            return receiver;
        } else if (!(receiver instanceof RosettaInterpreterTypedValue)) {
            return new RosettaInterpreterErrorValue(new RosettaInterpreterError(
                    "Receiver is not of type RosettaInterpreterTypedValue"));
        }
        return receiver;
    }
}

