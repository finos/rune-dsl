package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRuneScope implements RuneScope {
    @Inject
    private Injector injector;
    
    public AbstractRuneScope() {
        configure();
    }
    
    private final Map<Class<?>, Class<?>> overrides = new HashMap<>();
    
    protected abstract void configure();
    
    protected <T> void addOverride(Class<T> clazz, Class<? extends T> override) {
        if (!clazz.isAssignableFrom(override)) {
            throw new IllegalArgumentException("Override class " + override + " must be a subclass of the original class " + clazz);
        }
        overrides.put(clazz, override);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance((Class<? extends T>)overrides.getOrDefault(clazz, clazz));
    }
}
