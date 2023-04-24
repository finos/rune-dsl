package com.regnosys.rosetta.interpreter;

import org.apache.commons.lang3.Validate;

public abstract class RosettaValueItemWithNaturalOrder<T extends Comparable<? super T>> extends AbstractRosettaValueItem<T> implements Comparable<RosettaValueItem> {	
	public RosettaValueItemWithNaturalOrder(T value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(RosettaValueItem o) {
		Validate.notNull(o);
		if (this.getClass().equals(o.getClass())) {
			return getValue().compareTo((T)this.getClass().cast(o).getValue());
		}
		throw new RosettaInterpreterTypeException("Cannot compare an item of type " + this.getClass().getSimpleName() + "to an item of type " + o.getClass().getSimpleName() + ".");
	}
}
