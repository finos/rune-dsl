package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.FunctionContextAccess;

import javax.inject.Inject;

public class OneB extends One {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private One superFunction;

    public int evaluate() {
        return contextAccess.runInScope(ScopeB.class, () -> 3 * superFunction.evaluate());
    }
}
