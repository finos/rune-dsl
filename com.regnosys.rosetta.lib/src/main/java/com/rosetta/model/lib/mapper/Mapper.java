package com.rosetta.model.lib.mapper;

import java.util.List;
import java.util.Optional;

public interface Mapper<T> {
	
	T get();

	List<T> getMulti();
	
	Optional<?> getParent();
	
	List<?> getParentMulti();
	
	int resultCount();

	List<Path> getPaths();

	List<Path> getErrorPaths();
	
	List<String> getErrors();
	
	
	interface Path {
		List<String> getNames();
		List<String> getGetters();
		String getLastName();
		String getFullPath();
	}
}