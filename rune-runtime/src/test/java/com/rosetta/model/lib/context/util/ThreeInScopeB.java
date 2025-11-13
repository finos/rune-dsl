package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContext;
import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.InScope;

import javax.inject.Inject;

public class ThreeInScopeB {
    @Inject
    private FunctionContextAccess contextAccess;
    
    public int evaluate() {
        FunctionContext context = contextAccess.getContext();
        try (InScope ignored = context.inScope(ScopeB.class)) {
            ThreeInScopeA threeInScopeA = context.getInstanceInScope(ThreeInScopeA.class);
            return threeInScopeA.evaluate();
        }
    }
}
