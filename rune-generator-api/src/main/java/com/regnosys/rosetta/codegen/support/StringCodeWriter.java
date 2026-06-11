package com.regnosys.rosetta.codegen.support;

public class StringCodeWriter extends AbstractCodeWriter {
    private final StringBuilder builder = new StringBuilder();

    @Override
    protected void writeString(String str) {
        builder.append(str);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
