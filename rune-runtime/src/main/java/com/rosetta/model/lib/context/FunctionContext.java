package com.rosetta.model.lib.context;

import com.google.inject.ImplementedBy;

import java.util.function.Supplier;

/**
 * Provides a context for function execution with scoped overrides.
 */
@ImplementedBy(FunctionContextImpl.class)
public interface FunctionContext {
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
    <T> T evaluateInScope(Class<? extends FunctionScope> scopeClass, Supplier<T> supplier);

    /**
     * Gets an instance of the specified class, applying any active scope overrides.
     *
     * @param clazz the class to instantiate
     * @param <T> the type
     * @return an instance of the class (or its override)
     */
    <T> T getInstance(Class<T> clazz);

    /**
     * Creates a copy of the current thread's scope state for propagation to other threads.
     * <p>
     * Use this method to capture the current scope stack before spawning async tasks,
     * then call {@link #setStateOfCurrentThread(FunctionContextState)} in the new thread.
     *
     * @return a copy of the current thread's scope state
     */
    FunctionContextState copyStateOfCurrentThread();

    /**
     * Sets the current thread's scope state, typically after receiving it from another thread.
     * <p>
     * Use this method in a new thread to restore scope state that was captured
     * via {@link #copyStateOfCurrentThread()} in a parent thread.
     *
     * @param state the scope state to set for the current thread
     */
    void setStateOfCurrentThread(FunctionContextState state);
}
