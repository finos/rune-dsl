package com.rosetta.model.lib.path;

public class RosettaPathValue {
    private final RosettaPath path;
    private final Object value;

    public RosettaPathValue(RosettaPath path, Object value) {
        this.path = path;
        this.value = value;
    }

    @Override
    public String toString() {
        return "FieldValue{" +
                "path=" + path +
                ", value=" + value +
                '}';
    }

    public RosettaPath getPath() {
        return path;
    }

    public Object getValue() {
        return value;
    }
}
