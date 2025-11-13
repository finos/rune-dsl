package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.ScopedFunctionContext;

import javax.inject.Inject;

public class TwoA extends Two {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private Two two;
    
    public int evaluate() {
        try (ScopedFunctionContext context = contextAccess.getContextInScope(ScopeA.class)) {
            return 2 * two.evaluate();
        }
    }
}
