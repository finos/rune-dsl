package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;
import com.rosetta.model.lib.context.FunctionContextAccess;

import javax.inject.Inject;

public class ThreeInScopeA {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private ContextAwareProvider<Three> threeProvider;
    
    public int evaluate() {
        return contextAccess.runInScope(ScopeA.class, () -> {
            Three three = threeProvider.get();
            return three.evaluate();
        });
    }
}
