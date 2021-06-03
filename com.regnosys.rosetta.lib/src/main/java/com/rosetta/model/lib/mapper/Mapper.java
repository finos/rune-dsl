package com.rosetta.model.lib.mapper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface Mapper<T> {
	
	T get();

	List<T> getMulti();
	
	Optional<?> getParent();
	
	List<?> getParentMulti();
	
	int resultCount();

	List<Path> getPaths();

	List<Path> getErrorPaths();
	
	List<String> getErrors();
	
	static <T> Mapper<T> of(Supplier<Mapper<T>> supplier) {
		return supplier.get();
	}
	
	interface Path {
		List<String> getNames();
		List<String> getGetters();
		String getLastName();
		String getFullPath();
	}
}