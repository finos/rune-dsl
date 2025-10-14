package com.rosetta.model.lib.context;

public class Context {
    private final RuneScope scope;
    
    public Context(RuneScope scope) {
        this.scope = scope;
    }
    
    public RuneScope getScope() {
        return scope;
    }
}
