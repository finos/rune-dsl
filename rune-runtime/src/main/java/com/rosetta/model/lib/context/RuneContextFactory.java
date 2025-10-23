package com.rosetta.model.lib.context;

import javax.inject.Inject;

public class RuneContextFactory {
    @Inject
    private RuneScope.Default defaultScope;
    
    public RuneContext createDefault() {
        return withScope(defaultScope);
    }
    
    public RuneContext withScope(RuneScope scope) {
        return new RuneContext(scope);
    }
}
