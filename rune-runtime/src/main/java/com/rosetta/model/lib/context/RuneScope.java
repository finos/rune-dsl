package com.rosetta.model.lib.context;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

public interface RuneScope {
    <T> T getInstance(Class<T> clazz);
    
    @Singleton
    class Default implements RuneScope {
        @Inject
        private Injector injector;
        
        @Override
        public <T> T getInstance(Class<T> clazz) {
            return injector.getInstance(clazz);
        }
    }
}
