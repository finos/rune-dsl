package com.regnosys.rosetta.interpreter;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public abstract class RosettaValueWithNaturalOrder<T extends Comparable<? super T>> extends AbstractRosettaValue<T> {	
	private final Class<T> clazz;
	
	public RosettaValueWithNaturalOrder(List<T> items, Class<T> clazz) {
		super(items);
		Validate.notNull(clazz);
		this.clazz = clazz;
	}
	
	@Override
	public RosettaValueWithNaturalOrder<T> withNaturalOrderOrThrow() {
		return this;
	}
	
	public Optional<Comparable<Object>> getSingleComparable() {
		return getSingle().map(i -> new Comparable<Object>() {
				@Override
				public int compareTo(Object o) {
					if (!clazz.isInstance(o)) {
						throw new RosettaInterpreterTypeException("Cannot compare an item of type " + this.getClass().getSimpleName() + "to an item of type " + o.getClass().getSimpleName() + ".");
					}
					return i.compareTo(clazz.cast(o));
				}
			});
	}
	public Comparable<Object> getSingleComparableOrThrow() {
		T i = getSingleOrThrow();
		return new Comparable<Object>() {
			@Override
			public int compareTo(Object o) {
				if (!clazz.isInstance(o)) {
					throw new RosettaInterpreterTypeException("Cannot compare an item of type " + this.getClass().getSimpleName() + "to an item of type " + o.getClass().getSimpleName() + ".");
				}
				return i.compareTo(clazz.cast(o));
			}
		};
	}
}
