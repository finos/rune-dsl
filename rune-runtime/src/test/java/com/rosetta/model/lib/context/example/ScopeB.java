package com.rosetta.model.lib.context.example;

import com.rosetta.model.lib.context.AbstractFunctionScope;

import javax.inject.Singleton;

@Singleton
public class ScopeB extends AbstractFunctionScope {
    @Override
    protected void configure() {
        addOverride(One.class, OneB.class);
    }
}
