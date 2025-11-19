package com.regnosys.rosetta.generator.java.st;

import org.stringtemplate.v4.*;
import org.stringtemplate.v4.misc.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class RecordModelAdaptor implements ModelAdaptor<Record> {
    // Cache: record class -> (component name -> accessor handle)
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    
    private final Map<Class<?>, Map<String, MethodHandle>> cache = new ConcurrentHashMap<>();
    
    private final ObjectModelAdaptor<Record> fallback = new ObjectModelAdaptor<>();

    @Override
    public Object getProperty(Interpreter interp, ST self, Record o, Object property, String propertyName) throws STNoSuchPropertyException {
        Class<?> cls = o.getClass();
        // Fast path: only for real records
        if (!cls.isRecord()) {
            // delegate to default resolver for non-records
            return fallback.getProperty(interp, self, o, property, propertyName);
        }

        // Build or get cached map of component -> accessor handle
        Map<String, MethodHandle> byName = cache.computeIfAbsent(cls, c -> Arrays.stream(c.getRecordComponents())
                .collect(Collectors.toUnmodifiableMap(
                        RecordComponent::getName,
                        rc -> {
                            try {
                                Method m = rc.getAccessor();
                                m.setAccessible(true);
                                return LOOKUP.unreflect(m);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })));

        MethodHandle h = byName.get(propertyName);
        if (h != null) {
            try {
                return h.invoke(o); // call component accessor like name(), id(), etc.
            } catch (Throwable t) {
                Exception ex = (t instanceof Exception) ? (Exception) t : new Exception(t);
                throw new STNoSuchPropertyException(ex, o, propertyName);
            }
        }

        // Not a component: fall back to default (bean getter/field) resolution
        return fallback.getProperty(interp, self, o, property, propertyName);
    }
}

