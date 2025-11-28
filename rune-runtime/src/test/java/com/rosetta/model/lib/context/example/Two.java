package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;

import javax.inject.Inject;

public class Two {
    @Inject
    private ContextAwareProvider<One> oneProvider;
    
    public int evaluate() {
        One one = oneProvider.get();
        return 2 * one.evaluate();
    }
}
