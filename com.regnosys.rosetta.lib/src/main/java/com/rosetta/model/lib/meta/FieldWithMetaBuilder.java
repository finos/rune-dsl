package com.rosetta.model.lib.meta;

public interface FieldWithMetaBuilder<T> {
	FieldWithMetaBuilder<T> setValue(T value);
	Object getValue();
	
	Class<T> getValueType();
}
