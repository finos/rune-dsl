package com.rosetta.model.lib.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the per-thread state of a {@link FunctionContext}.
 * <p>
 * This class is separated from {@link FunctionContext} to enable explicit propagation of context
 * state across thread boundaries. Instances can be copied via {@link #copy()} and transferred
 * to other threads, allowing async tasks to inherit the parent thread's context.
 * <p>
 * Maintains a scope stack with cached resolved overrides for O(1) lookup performance.
 */
public class FunctionContextState {
    private static class ScopeFrame {
        final FunctionScope scope;
        final Map<Class<?>, Class<?>> resolvedOverrides;

        ScopeFrame(FunctionScope scope, Map<Class<?>, Class<?>> resolvedOverrides) {
            this.scope = scope;
            this.resolvedOverrides = resolvedOverrides;
        }
    }
    private final Deque<ScopeFrame> scopeStack;
    
    public static FunctionContextState empty() {
        return new FunctionContextState();
    }
    
    private FunctionContextState(FunctionContextState otherState) {
        this.scopeStack = new ArrayDeque<>(otherState.scopeStack);
    }
    private FunctionContextState() {
        this.scopeStack = new ArrayDeque<>();
    }

    /**
     * Creates a shallow copy of this state for propagation to another thread.
     * <p>
     * The scope stack is copied, so modifications in one thread won't affect the other.
     *
     * @return a copy of this state
     */
    public FunctionContextState copy() {
        return new FunctionContextState(this);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getOverride(Class<T> clazz) {
        // O(1) cache lookup from the top of the stack
        if (scopeStack.isEmpty()) {
            return clazz;
        }
        Map<Class<?>, Class<?>> resolvedOverrides = scopeStack.peekLast().resolvedOverrides;
        return (Class<? extends T>) resolvedOverrides.getOrDefault(clazz, clazz);
    }

    /**
     * Pushes a new scope onto the stack, if the scope is different from the last.
     * @param scope the scope to push
     * @return true if the scope was pushed, false if the scope equals the top of the stack
     */
    public boolean pushScope(FunctionScope scope) {
        if (!scopeStack.isEmpty() && scopeStack.peekLast().scope.equals(scope)) {
            return false;
        }
        
        // Get the current resolved overrides (from parent scope or empty)
        Map<Class<?>, Class<?>> parentResolvedOverrides = scopeStack.isEmpty()
                ? new HashMap<>()
                : scopeStack.peekLast().resolvedOverrides;

        // Apply new scope's overrides to all classes in the parent overrides.
        // This ensures that if class A was already overridden to B,
        // and the new scope overrides B to C, then A will resolve to C.
        Map<Class<?>, Class<?>> newResolvedOverrides = new HashMap<>();
        for (Map.Entry<Class<?>, Class<?>> entry : parentResolvedOverrides.entrySet()) {
            Class<?> baseClass = entry.getKey();
            Class<?> currentOverride = entry.getValue();
            Class<?> newOverride = scope.getOverride(currentOverride);
            newResolvedOverrides.put(baseClass, newOverride);
        }

        // Also add any new overrides from this scope that aren't in the cache yet.
        Map<Class<?>, Class<?>> scopeOverrides = scope.getAllOverrides();
        for (Map.Entry<Class<?>, Class<?>> entry : scopeOverrides.entrySet()) {
            Class<?> baseClass = entry.getKey();
            if (!newResolvedOverrides.containsKey(baseClass)) {
                newResolvedOverrides.put(baseClass, entry.getValue());
            }
        }

        scopeStack.addLast(new ScopeFrame(scope, newResolvedOverrides));
        return true;
    }
    
    public void popScope() {
        scopeStack.removeLast();
    }
}
