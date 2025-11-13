package com.rosetta.model.lib.context;

public interface FunctionScope {
    <T> Class<? extends T> getOverride(Class<T> clazz);
}
