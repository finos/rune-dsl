package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.ContextAwareProvider;

import javax.inject.Inject;

public class Three {
    @Inject
    private ContextAwareProvider<One> oneProvider;
    @Inject
    private ContextAwareProvider<Two> twoProvider;
    
    public int evaluate() {
        One one = oneProvider.get();
        Two two = twoProvider.get();
        return one.evaluate() + two.evaluate();
    }
}
