package com.rosetta.model.lib.context;

import com.google.inject.TypeLiteral;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A provider that resolves instances based on the current function context and its scopes.
 * <p>
 * This provider can be injected into classes to enable scope-aware dependency resolution.
 * Instead of receiving a fixed instance at injection time, the instance is resolved dynamically
 * when {@link #get()} is called, taking into account any active scopes on the current thread.
 * <p>
 * Example usage:
 * <pre>
 * public class MyClass {
 *     {@literal @}Inject
 *     private ContextAwareProvider&lt;MyDependency&gt; dependencyProvider;
 *
 *     public void doWork() {
 *         MyDependency dep = dependencyProvider.get(); // Resolved based on current scope
 *         // ... use dep
 *     }
 * }
 * </pre>
 *
 * @param <T> the type to provide
 */
public class ContextAwareProvider<T> implements Provider<T>, jakarta.inject.Provider<T> {
    private final TypeLiteral<T> type;
    private final FunctionContext context;

    @Inject
    public ContextAwareProvider(TypeLiteral<T> type,
                                FunctionContext context) {
        this.type = type;
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        Class<? extends T> raw = (Class<? extends T>) type.getRawType();
        return context.getInstance(raw);
    }
}
