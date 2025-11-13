package com.rosetta.model.lib.context;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFunctionScope implements FunctionScope {
    private final Map<Class<?>, Class<?>> overrides = new HashMap<>();

    public AbstractFunctionScope() {
        configure();
    }
    
    protected abstract void configure();

    protected <T> void addOverride(Class<T> clazz, Class<? extends T> override) {
        if (!clazz.isAssignableFrom(override)) {
            throw new IllegalArgumentException("Override class " + override + " must be a subclass of the original class " + clazz);
        }
        overrides.put(clazz, override);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getOverride(Class<T> clazz) {
        return (Class<? extends T>) overrides.getOrDefault(clazz, clazz);
    }
}
