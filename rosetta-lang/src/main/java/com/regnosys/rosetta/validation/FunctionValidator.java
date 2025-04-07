package com.regnosys.rosetta.validation;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Function;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    
    @Check
    public void warnWhenEmptyFunctionsDontHaveCodeImplementationAnnotation(Function function) {
        
        
    }

}
