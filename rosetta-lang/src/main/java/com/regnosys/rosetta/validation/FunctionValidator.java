package com.regnosys.rosetta.validation;

import org.eclipse.xtext.validation.Check;
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Function;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    
    @Check
    public void warnWhenEmptyFunctionsDontHaveCodeImplementationAnnotation(Function function) {
        Annotated annotated = (Annotated) function;
        boolean hasCodeImplementationAnnotation = annotated.getAnnotations()
        .stream()
        .map(aRef -> aRef.getAnnotation())
        .anyMatch(a -> "codeImplementation".equals(a.getName()));
        
        if (function.getOperations().isEmpty() && !hasCodeImplementationAnnotation) {
            warning("Functions with no Rune implementation should be annotated with codeImplementation", function, ROSETTA_NAMED__NAME);
        }
        
        if (!function.getOperations().isEmpty() && hasCodeImplementationAnnotation) {
            warning("Functions annotated with codeImplementation should not have any setter operations as they will be overriden", function, ROSETTA_NAMED__NAME);
        }
    }

}
