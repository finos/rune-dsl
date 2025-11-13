package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FunctionContextAccess {
    private final ThreadLocal<FunctionContext> contextPerThread = ThreadLocal.withInitial(this::createEmptyContext);
    private final Injector injector;
    
    @Inject
    public FunctionContextAccess(Injector injector) {
        this.injector = injector;
    }
    
    public FunctionContext getContext() {
        return contextPerThread.get();
    }
    public ScopedFunctionContext getContextInScope(Class<? extends FunctionScope> scopeClass) {
        return getContext().inScope(scopeClass);
    }
    
    private FunctionContext createEmptyContext() {
        return new ScopedFunctionContext(injector);
    }
}
