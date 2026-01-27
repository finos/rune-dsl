package com.rosetta.model.lib.context;

import java.util.Map;

/**
 * Defines a scope with class binding overrides.
 * <p>
 * Implementations of this interface specify which classes should be replaced
 * with alternative implementations within a particular execution context.
 */
public interface FunctionScope {
    /**
     * Returns the override class for the given class, or the class itself if no override exists.
     *
     * @param clazz the class to check for an override
     * @param <T> the type of the class
     * @return the override class if one exists, otherwise the original class. Never null.
     */
    <T> Class<? extends T> getOverride(Class<T> clazz);

    /**
     * Returns all class overrides defined in this scope.
     *
     * @return a map from original classes to their override classes
     */
    Map<Class<?>, Class<?>> getAllOverrides();
}
