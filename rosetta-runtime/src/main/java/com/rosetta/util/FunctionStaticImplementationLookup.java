package com.rosetta.util;

import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.rosetta.model.lib.functions.RosettaFunction;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

public interface FunctionStaticImplementationLookup {
    boolean hasStaticImplementation(String fucntion);


    class Default implements FunctionStaticImplementationLookup {
        private final Injector injector;

        @Inject
        public Default(Injector injector) {
            this.injector = injector;
        }
        
        public boolean hasStaticImplementation(Class<?> function) {

            Object instance = injector.getInstance(function);
            Annotation[] annotations = function.getAnnotations();
            Optional<Boolean> isStatic = Arrays.stream(annotations)
                    .filter(a -> a.annotationType().equals(ImplementedBy.class))
                    .findFirst()
                    .map(a -> {
                        ImplementedBy annotation = (ImplementedBy) a;
                        Class<?> defaultImplementation = annotation.value();
                        return !defaultImplementation.isAssignableFrom(instance.getClass());
                    });
            return isStatic.orElse(false);
        }

        @Override
        public boolean hasStaticImplementation(String fucntion) {
            Class<?> functionClass;
            try {
                functionClass = this.getClass().getClassLoader().loadClass(fucntion);
            } catch (ClassNotFoundException e) {
                return false;
            }
            return hasStaticImplementation(functionClass);
        }
        
    }
}
