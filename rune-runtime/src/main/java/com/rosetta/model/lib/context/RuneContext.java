package com.rosetta.model.lib.context;

public class RuneContext {
    private final RuneScope scope;
    
    protected RuneContext(RuneScope scope) {
        this.scope = scope;
    }
    
    public RuneScope getScope() {
        return scope;
    }
}
