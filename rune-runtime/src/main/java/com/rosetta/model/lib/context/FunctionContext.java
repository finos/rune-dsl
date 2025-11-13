package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import java.util.ArrayDeque;
import java.util.Deque;

public class FunctionContext {
    private final Deque<FunctionScope> scopeStack = new ArrayDeque<>();
    private final Injector injector;
    
    public FunctionContext(Injector injector) {
        this.injector = injector;
    }
    
    public InScope inScope(Class<? extends FunctionScope> scopeClass) {
        FunctionScope scope = injector.getInstance(scopeClass);
        scopeStack.addLast(scope);
        return scopeStack::removeLast;
    }
    
    public <T> T getInstanceInScope(Class<T> clazz) {
        Class<? extends T> current = clazz;
        for (FunctionScope scope : scopeStack) {
            current = scope.getOverride(current);
        }
        return injector.getInstance(current);
    }
}
