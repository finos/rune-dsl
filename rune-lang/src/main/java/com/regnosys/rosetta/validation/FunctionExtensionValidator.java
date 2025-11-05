package com.regnosys.rosetta.validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaScope;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;

import java.util.*;

import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.FUNCTION__SUPER_FUNCTION;

public class FunctionExtensionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private CycleValidationHelper cycleValidationHelper;
    
    @Check
    public void checkFunctionExtensionMustBeInScopedFile(Function function) {
        if (function.getSuperFunction() == null) return;
        RosettaModel model = function.getModel();
        if (model != null && model.getScope() == null) {
            error("You can only extend a function in a file with a scope", function, SimplePackage.Literals.FUNCTION__SUPER_FUNCTION);
        }
    }
    
    @Check
    public void checkFunctionInScopeIsExtendedAtMostOnce(RosettaModel model) {
        RosettaScope scope = model.getScope();
        if (scope == null) return;

        Multimap<Function, Function> functionExtensions = HashMultimap.create();
        for (RosettaRootElement elem : model.getElements()) {
            if (!(elem instanceof Function function)) continue;
            if (function.getSuperFunction() != null) {
                functionExtensions.put(function.getSuperFunction(), function);
            }
        }
        for (Function function : functionExtensions.keySet()) {
            Collection<Function> extensions = functionExtensions.get(function);
            if (extensions.size() > 1) {
                for (Function extension : extensions) {
                    error("Function '" + function.getName() + "' is extended multiple times in scope " + scope.getName(), extension, SimplePackage.Literals.FUNCTION__SUPER_FUNCTION);
                }
            }
        }
    }

    @Check
    public void checkCyclicExtensions(Function func) {
        cycleValidationHelper.detectCycle(
                func,
                Function::getSuperFunction,
                "extends",
                (pathMsg) -> error("Cyclic extension: " + pathMsg, func, FUNCTION__SUPER_FUNCTION)
        );
    }
    
    @Check
    public void checkInputAndOutputsOfFunctionExtensionAreTheSameAsOriginal(Function function) {
        Function original = function.getSuperFunction();
        if (original == null) return;
        
        if (function.getInputs().size() < original.getInputs().size()) {
            error("Function " + function.getName() + " does not define all inputs of the original function " + original.getName(), function, SimplePackage.Literals.FUNCTION__SUPER_FUNCTION);
        }
        for (int i = 0; i < function.getInputs().size(); i++) {
            if (i >= original.getInputs().size()) {
                error("Too many inputs. The original function " + original.getName() + " only defines " + original.getInputs().size() + " inputs.", function, SimplePackage.Literals.FUNCTION__INPUTS, i);
            } else {
                checkEqual(function.getInputs().get(i), original.getInputs().get(i), "input", function, SimplePackage.Literals.FUNCTION__INPUTS, i);
            }
        }
        
        checkEqual(function.getOutput(), original.getOutput(), "output", function, SimplePackage.Literals.FUNCTION__OUTPUT, INSIGNIFICANT_INDEX);
    }
    private void checkEqual(Attribute toCheck, Attribute expected, String description, Function function, EReference feature, int index) {
        if (toCheck == null || expected == null) return;
        
        if (!EcoreUtil2.equals(toCheck, expected)) {
            error(StringUtils.capitalize(description) + " " + toCheck.getName() + " does not match the original " + description + " in " + function.getSuperFunction().getName(), function, feature, index);
        }
    }
}
