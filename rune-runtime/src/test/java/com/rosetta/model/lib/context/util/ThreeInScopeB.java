package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.ScopedFunctionContext;

import javax.inject.Inject;

public class ThreeInScopeB {
    @Inject
    private FunctionContextAccess contextAccess;
    
    public int evaluate() {
        try (ScopedFunctionContext context = contextAccess.getContextInScope(ScopeB.class)) {
            ThreeInScopeA threeInScopeA = context.getInstanceInScope(ThreeInScopeA.class);
            return threeInScopeA.evaluate();
        }
    }
}
