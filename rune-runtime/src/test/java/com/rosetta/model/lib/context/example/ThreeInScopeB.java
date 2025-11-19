package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;
import com.rosetta.model.lib.context.FunctionContext;

import javax.inject.Inject;

public class ThreeInScopeB {
    @Inject
    private FunctionContext context;
    @Inject
    private ContextAwareProvider<ThreeInScopeA> threeInScopeAProvider;
    
    public int evaluate() {
        return context.evaluateInScope(ScopeB.class, () -> {
            ThreeInScopeA threeInScopeA = threeInScopeAProvider.get();
            return threeInScopeA.evaluate();
        });
    }
}
