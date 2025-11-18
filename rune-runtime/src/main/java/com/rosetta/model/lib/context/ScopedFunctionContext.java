package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Implementation of {@link FunctionContext} that maintains a stack of scopes with cached resolved overrides.
 * <p>
 * This implementation optimizes {@link #getInstanceInScope(Class)} to O(1) time complexity by maintaining
 * a cache of resolved class overrides at each scope level. When a scope is entered, the cache is computed
 * by applying that scope's overrides to the parent scope's cache. When a scope is exited, the parent's
 * cache is automatically restored by popping the stack.
 * <p>
 * This class is {@link AutoCloseable} to support try-with-resources blocks for scope management.
 */
public class ScopedFunctionContext implements FunctionContext, AutoCloseable {
    private static class ScopeFrame {
        final FunctionScope scope;
        final Map<Class<?>, Class<?>> resolvedOverrides;

        ScopeFrame(FunctionScope scope, Map<Class<?>, Class<?>> resolvedOverrides) {
            this.scope = scope;
            this.resolvedOverrides = resolvedOverrides;
        }
    }

    private final Deque<ScopeFrame> scopeStack = new ArrayDeque<>();
    private final Injector injector;
    
    public ScopedFunctionContext(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ScopedFunctionContext inScope(Class<? extends FunctionScope> scopeClass) {
        FunctionScope scope = injector.getInstance(scopeClass);

        // Get the current resolved overrides (from parent scope or empty)
        Map<Class<?>, Class<?>> parentResolvedOverrides = scopeStack.isEmpty()
            ? new HashMap<>()
            : scopeStack.peekLast().resolvedOverrides;

        // Apply new scope's overrides to all classes in the parent cache
        // This ensures that if class A was already overridden to B,
        // and the new scope overrides B to C, then A will resolve to C
        Map<Class<?>, Class<?>> newResolvedOverrides = new HashMap<>();
        for (Map.Entry<Class<?>, Class<?>> entry : parentResolvedOverrides.entrySet()) {
            Class<?> baseClass = entry.getKey();
            Class<?> currentOverride = entry.getValue();
            Class<?> newOverride = scope.getOverride(currentOverride);
            newResolvedOverrides.put(baseClass, newOverride);
        }

        // Also add any new overrides from this scope that aren't in the cache yet
        Map<Class<?>, Class<?>> scopeOverrides = scope.getAllOverrides();
        for (Map.Entry<Class<?>, Class<?>> entry : scopeOverrides.entrySet()) {
            Class<?> baseClass = entry.getKey();
            if (!newResolvedOverrides.containsKey(baseClass)) {
                newResolvedOverrides.put(baseClass, entry.getValue());
            }
        }

        scopeStack.addLast(new ScopeFrame(scope, newResolvedOverrides));

        return this;
    }

    @Override
    public void runInScope(Class<? extends FunctionScope> scopeClass, Runnable runnable) {
        try (ScopedFunctionContext scopedContext = inScope(scopeClass)) {
            runnable.run();
        }
    }

    @Override
    public <T> T runInScope(Class<? extends FunctionScope> scopeClass, Supplier<T> supplier) {
        try (ScopedFunctionContext scopedContext = inScope(scopeClass)) {
            return supplier.get();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstanceInScope(Class<T> clazz) {
        // O(1) cache lookup from the top of the stack
        if (scopeStack.isEmpty()) {
            return injector.getInstance(clazz);
        }
        Map<Class<?>, Class<?>> resolvedOverrides = scopeStack.peekLast().resolvedOverrides;
        Class<? extends T> resolvedClass = (Class<? extends T>) resolvedOverrides.getOrDefault(clazz, clazz);
        return injector.getInstance(resolvedClass);
    }
    
    /**
     * Closes this scope context by removing the most recent scope from the stack.
     * <p>
     * This automatically restores the parent scope's resolved overrides. After closing,
     * any subsequent calls to {@link #getInstanceInScope(Class)} will use the parent
     * scope's overrides (or no overrides if this was the last scope).
     * <p>
     * This method is called automatically when using try-with-resources blocks.
     */
    @Override
    public void close() {
        if (!scopeStack.isEmpty()) {
            scopeStack.removeLast();
        }
    }
}
