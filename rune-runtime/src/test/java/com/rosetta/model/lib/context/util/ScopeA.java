package com.rosetta.model.lib.context.util;

import com.rosetta.model.lib.context.AbstractFunctionScope;

import javax.inject.Singleton;

@Singleton
public class ScopeA extends AbstractFunctionScope {
    @Override
    protected void configure() {
        addOverride(Two.class, TwoA.class);
    }
}
