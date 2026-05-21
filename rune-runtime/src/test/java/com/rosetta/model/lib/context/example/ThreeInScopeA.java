package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;
import com.rosetta.model.lib.context.FunctionContext;

import javax.inject.Inject;

public class ThreeInScopeA {
    @Inject
    private FunctionContext context;
    @Inject
    private ContextAwareProvider<Three> threeProvider;
    
    public int evaluate() {
        return context.evaluateInScope(ScopeA.class, () -> {
            Three three = threeProvider.get();
            return three.evaluate();
        });
    }
}
