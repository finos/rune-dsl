package com.rosetta.model.lib.meta;

public interface FieldWithMeta<T> {
	T getValue();
	Class<T> getValueType();
}
