package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.rosetta.util.FunctionStaticImplementationLookup;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private FunctionStaticImplementationLookup functionStaticImplementationLookup;
    
    
    @Check
    public void checkStaticFunctionHasImplementation(Function function) {
        if (function instanceof Annotated) {
            var annotated = (Annotated) function;
            if (annotated.getAnnotations()
                .stream()
                .map(aRef -> aRef.getAnnotation())
                .anyMatch(a -> "staticImplementation".equals(a.getName()))) {
                
                if (functionStaticImplementationLookup.hasStaticImplementation(function.getName()) ) {
                    //TODO: warn here
                }
            }
                
        }
    }
    

}
