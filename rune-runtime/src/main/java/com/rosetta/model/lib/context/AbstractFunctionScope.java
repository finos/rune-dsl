package com.rosetta.model.lib.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for implementing {@link FunctionScope}.
 * <p>
 * Subclasses should override {@link #configure()} and call {@link #addOverride(Class, Class)}
 * to define the class overrides for this scope.
 * <p>
 * Example:
 * <pre>
 * public class MyScope extends AbstractFunctionScope {
 *     protected void configure() {
 *         addOverride(BaseClass.class, OverrideClass.class);
 *     }
 * }
 * </pre>
 */
public abstract class AbstractFunctionScope implements FunctionScope {
    private final Map<Class<?>, Class<?>> overrides = new HashMap<>();

    public AbstractFunctionScope() {
        configure();
    }

    /**
     * Configures this scope by adding class overrides.
     * <p>
     * Subclasses must implement this method to define their overrides using {@link #addOverride(Class, Class)}.
     */
    protected abstract void configure();

    /**
     * Adds a class override to this scope.
     *
     * @param clazz the original class
     * @param override the override class (must be a subclass of the original class)
     * @param <T> the type of the class
     * @throws IllegalArgumentException if the override is not a subclass of the original class,
     *                                  or if the class already has an override defined
     */
    protected <T> void addOverride(Class<T> clazz, Class<? extends T> override) {
        if (!clazz.isAssignableFrom(override)) {
            throw new IllegalArgumentException("Override class " + override + " must be a subclass of the original class " + clazz);
        }
        if (overrides.containsKey(clazz)) {
            throw new IllegalArgumentException("Class " + clazz + " is already overridden by " + overrides.get(clazz));
        }
        overrides.put(clazz, override);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getOverride(Class<T> clazz) {
        return (Class<? extends T>) overrides.getOrDefault(clazz, clazz);
    }

    @Override
    public Map<Class<?>, Class<?>> getAllOverrides() {
        return overrides;
    }
}
