package com.regnosys.rosetta.validation;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Function;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    
    
    @Check
    public void checkStaticFunctionHasImplementation(Function function) {
        if (function instanceof Annotated) {
            var annotated = (Annotated) function;
            if (annotated.getAnnotations()
                .stream()
                .map(aRef -> aRef.getAnnotation())
                .anyMatch(a -> "staticImplementation".equals(a.getName()))) {
                
                System.out.println("foo");
            }
                
        }
    }
    

}
