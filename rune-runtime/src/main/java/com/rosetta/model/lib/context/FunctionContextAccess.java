package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * Thread-safe access to {@link FunctionContext}.
 * <p>
 * Maintains a separate context per thread for concurrent execution.
 */
@Singleton
public class FunctionContextAccess {
    private final ThreadLocal<FunctionContext> contextPerThread = ThreadLocal.withInitial(this::createEmptyContext);
    private final Injector injector;

    @Inject
    public FunctionContextAccess(Injector injector) {
        this.injector = injector;
    }

    /**
     * Returns the context for the current thread.
     *
     * @return the current thread's context
     */
    public FunctionContext getContext() {
        return contextPerThread.get();
    }

    /**
     * @see FunctionContext#inScope(Class)
     */
    public ScopedFunctionContext getContextInScope(Class<? extends FunctionScope> scopeClass) {
        return getContext().inScope(scopeClass);
    }

    /**
     * @see FunctionContext#runInScope(Class, Runnable)
     */
    public void runInScope(Class<? extends FunctionScope> scopeClass, Runnable runnable) {
        getContext().runInScope(scopeClass, runnable);
    }

    /**
     * @see FunctionContext#runInScope(Class, Supplier)
     */
    public <T> T runInScope(Class<? extends FunctionScope> scopeClass, Supplier<T> supplier) {
        return getContext().runInScope(scopeClass, supplier);
    }

    private FunctionContext createEmptyContext() {
        return new ScopedFunctionContext(injector);
    }
}
