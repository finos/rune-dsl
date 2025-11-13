package com.rosetta.model.lib.context;

@FunctionalInterface
public interface InScope extends AutoCloseable {
    @Override
    void close();
}
