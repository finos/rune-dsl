package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScopedFunctionContext implements FunctionContext, AutoCloseable {
    private final Deque<FunctionScope> scopeStack = new ArrayDeque<>();
    private final Injector injector;

    public ScopedFunctionContext(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ScopedFunctionContext inScope(Class<? extends FunctionScope> scopeClass) {
        FunctionScope scope = injector.getInstance(scopeClass);
        scopeStack.addLast(scope);
        return this;
    }

    @Override
    public <T> T getInstanceInScope(Class<T> clazz) {
        Class<? extends T> current = clazz;
        for (FunctionScope scope : scopeStack) {
            current = scope.getOverride(current);
        }
        return injector.getInstance(current);
    }
    
    @Override
    public void close() {
        scopeStack.removeLast();
    }
}
