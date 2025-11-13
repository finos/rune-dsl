package com.rosetta.model.lib.context;

public interface FunctionContext {
    ScopedFunctionContext inScope(Class<? extends FunctionScope> scopeClass);
    
    <T> T getInstanceInScope(Class<T> clazz);
}
