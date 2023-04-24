package com.regnosys.rosetta.interpreter;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public abstract class AbstractRosettaValueItem<T> implements RosettaValueItem {
	private T value;
	
	public AbstractRosettaValueItem(T value) {
		Validate.notNull(value);
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
        AbstractRosettaValueItem<T> other = this.getClass().cast(object);
        return Objects.equals(value, other.value);
	}
}
