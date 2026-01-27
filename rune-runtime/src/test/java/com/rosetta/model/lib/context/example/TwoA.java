package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.FunctionContext;

import javax.inject.Inject;

public class TwoA extends Two {
    @Inject
    private FunctionContext context;
    @Inject
    private Two superFunction;
    
    public int evaluate() {
        return context.evaluateInScope(ScopeA.class, () -> 2 * superFunction.evaluate());
    }
}
