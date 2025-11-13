package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.ScopedFunctionContext;

import javax.inject.Inject;

public class ThreeInScopeA {
    @Inject
    private FunctionContextAccess contextAccess;
    
    public int evaluate() {
        try (ScopedFunctionContext context = contextAccess.getContextInScope(ScopeA.class)) {
            Three three = context.getInstanceInScope(Three.class);
            return three.evaluate();
        }
    }
}
