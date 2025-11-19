package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.FunctionContext;

import javax.inject.Inject;

public class OneB extends One {
    @Inject
    private FunctionContext context;
    @Inject
    private One superFunction;

    public int evaluate() {
        return context.evaluateInScope(ScopeB.class, () -> 3 * superFunction.evaluate());
    }
}
