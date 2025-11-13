package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContext;
import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.InScope;

import javax.inject.Inject;

public class OneB extends One {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private One one;

    public int evaluate() {
        FunctionContext context = contextAccess.getContext();
        try (InScope ignored = context.inScope(ScopeB.class)) {
            return 3 * one.evaluate();
        }
    }
}
