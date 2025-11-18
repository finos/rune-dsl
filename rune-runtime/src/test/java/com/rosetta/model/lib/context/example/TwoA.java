package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.FunctionContextAccess;

import javax.inject.Inject;

public class TwoA extends Two {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private Two superFunction;
    
    public int evaluate() {
        return contextAccess.runInScope(ScopeA.class, () -> 2 * superFunction.evaluate());
    }
}
