package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContext;
import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.InScope;

import javax.inject.Inject;

public class ThreeInScopeA {
    @Inject
    private FunctionContextAccess contextAccess;
    
    public int evaluate() {
        FunctionContext context = contextAccess.getContext();
        try (InScope ignored = context.inScope(ScopeA.class)) {
            Three three = context.getInstanceInScope(Three.class);
            return three.evaluate();
        }
    }
}
