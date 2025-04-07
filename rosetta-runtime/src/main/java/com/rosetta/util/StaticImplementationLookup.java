package com.rosetta.util;

import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.rosetta.model.lib.functions.RosettaFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.Optional;

public interface StaticImplementationLookup {
    boolean hasStaticImplementation(Class<? extends RosettaFunction> fucntion);

    class Default implements StaticImplementationLookup {
        private final Injector injector;

        @Inject
        public Default(Injector injector) {
            this.injector = injector;
        }

        @Override
        public boolean hasStaticImplementation(Class<? extends RosettaFunction> function) {
            RosettaFunction instance = injector.getInstance(function);
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
        
    }
}
