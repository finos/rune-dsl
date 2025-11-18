package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;
import com.rosetta.model.lib.context.FunctionContextAccess;

import javax.inject.Inject;

public class ThreeInScopeB {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private ContextAwareProvider<ThreeInScopeA> threeInScopeAProvider;
    
    public int evaluate() {
        return contextAccess.runInScope(ScopeB.class, () -> {
            ThreeInScopeA threeInScopeA = threeInScopeAProvider.get();
            return threeInScopeA.evaluate();
        });
    }
}
