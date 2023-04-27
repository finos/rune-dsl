package com.regnosys.rosetta.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface RosettaValue {
	public List<?> getItems();
	public <U> List<U> getItems(Class<U> clazz);
	public RosettaValueWithNaturalOrder<?> withNaturalOrderOrThrow();
	
	public int size();
	public Stream<?> stream();
	
	public Optional<?> getSingle();
	public Object getSingleOrThrow();
	public <U> Optional<U> getSingle(Class<U> clazz);
	public <U> U getSingleOrThrow(Class<U> clazz);
	
	
	public static RosettaValue empty() {
		return new RosettaValueWithNaturalOrder</*Dummy class*/Integer>(Collections.emptyList(), Integer.class) {};
	}
}
