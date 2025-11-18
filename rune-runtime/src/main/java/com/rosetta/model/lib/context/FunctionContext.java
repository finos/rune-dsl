package com.rosetta.model.lib.context;

import java.util.function.Supplier;

/**
 * Provides a context for function execution with scoped overrides.
 */
public interface FunctionContext {
    /**
     * Enters a {@link FunctionScope}, returning a new context with its overrides applied.
     *
     * @param scopeClass the scope to enter
     * @return a new context with the scope applied
     */
    ScopedFunctionContext inScope(Class<? extends FunctionScope> scopeClass);

    /**
     * Executes code within a {@link FunctionScope}.
     *
     * @param scopeClass the scope to use
     * @param runnable the code to execute
     */
    void runInScope(Class<? extends FunctionScope> scopeClass, Runnable runnable);

    /**
     * Executes code within a {@link FunctionScope} and returns the result.
     *
     * @param scopeClass the scope to use
     * @param supplier the code to execute
     * @param <T> the return type
     * @return the result
     */
    <T> T runInScope(Class<? extends FunctionScope> scopeClass, Supplier<T> supplier);

    /**
     * Gets an instance of the specified class, applying any active scope overrides.
     *
     * @param clazz the class to instantiate
     * @param <T> the type
     * @return an instance of the class (or its override)
     */
    <T> T getInstanceInScope(Class<T> clazz);
}
