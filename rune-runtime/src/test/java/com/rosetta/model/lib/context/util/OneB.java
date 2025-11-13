package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.ScopedFunctionContext;

import javax.inject.Inject;

public class OneB extends One {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private One one;

    public int evaluate() {
        try (ScopedFunctionContext context = contextAccess.getContextInScope(ScopeB.class)) {
            return 3 * one.evaluate();
        }
    }
}
