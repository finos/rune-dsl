package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.FunctionContext;
import com.rosetta.model.lib.context.FunctionContextAccess;

import javax.inject.Inject;

public class Two {
    @Inject
    private FunctionContextAccess contextAccess;
    
    public int evaluate() {
        FunctionContext context = contextAccess.getContext();
        One one = context.getInstanceInScope(One.class);
        return 2 * one.evaluate();
    }
}
