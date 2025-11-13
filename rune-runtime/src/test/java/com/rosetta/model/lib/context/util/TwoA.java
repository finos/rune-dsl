package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContext;
import com.rosetta.model.lib.context.FunctionContextAccess;
import com.rosetta.model.lib.context.InScope;

import javax.inject.Inject;

public class TwoA extends Two {
    @Inject
    private FunctionContextAccess contextAccess;
    @Inject
    private Two two;
    
    public int evaluate() {
        FunctionContext context = contextAccess.getContext();
        try (InScope ignored = context.inScope(ScopeA.class)) {
            return 2 * two.evaluate();
        }
    }
}
