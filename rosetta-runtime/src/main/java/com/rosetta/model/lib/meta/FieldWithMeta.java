package com.rosetta.model.lib.meta;

public interface FieldWithMeta<T> {
	T getValue();
	Class<T> getValueType();
	
	interface FieldWithMetaBuilder<T> extends FieldWithMeta<T> {
		FieldWithMetaBuilder<T> setValue(T value);
	}
}
