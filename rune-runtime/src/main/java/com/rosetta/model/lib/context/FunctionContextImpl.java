package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * Implementation of {@link FunctionContext} that maintains a stack of scopes with cached resolved overrides.
 * <p>
 * This implementation optimizes {@link #getInstance(Class)} to O(1) time complexity (with respect to the depth of the scope stack)
 * by maintaining a cache of resolved class overrides at each scope level. When a scope is entered, the cache is computed
 * by applying that scope's overrides to the parent scope's cache. When a scope is exited, the parent's
 * cache is automatically restored by popping the stack.
 */
@Singleton
public class FunctionContextImpl implements FunctionContext {
    private final ThreadLocal<FunctionContextState> statePerThread = ThreadLocal.withInitial(FunctionContextState::empty);
    private final Injector injector;
    
    @Inject
    public FunctionContextImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void runInScope(Class<? extends FunctionScope> scopeClass, Runnable runnable) {
        if (!pushScope(scopeClass)) {
            runnable.run();
            return;
        }
        try {
            runnable.run();
        } finally {
            popScope();
        }
    }

    @Override
    public <T> T evaluateInScope(Class<? extends FunctionScope> scopeClass, Supplier<T> supplier) {
        if (!pushScope(scopeClass)) {
            return supplier.get();
        };
        try {
            return supplier.get();
        } finally {
            popScope();
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        Class<? extends T> resolvedClass = statePerThread.get().getOverride(clazz);
        return injector.getInstance(resolvedClass);
    }

    @Override
    public FunctionContextState copyStateOfCurrentThread() {
        return statePerThread.get().copy();
    }

    @Override
    public void setStateOfCurrentThread(FunctionContextState state) {
        statePerThread.set(state);
    }

    private boolean pushScope(Class<? extends FunctionScope> scopeClass) {
        FunctionScope scope = injector.getInstance(scopeClass);
        return statePerThread.get().pushScope(scope);
    }
    
    private void popScope() {
        statePerThread.get().popScope();
    }
}
