package com.rosetta.model.lib.context;

import javax.inject.Inject;

public class ContextFactory {
    @Inject
    private RuneScope.Default defaultScope;
    
    public Context createDefault() {
        return withScope(defaultScope);
    }
    
    public Context withScope(RuneScope scope) {
        return new Context(scope);
    }
}
