package com.rosetta.model.lib.mapper;

import java.util.function.Supplier;

public class MapperUtils {
	
	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> Mapper<T> from(Supplier<Mapper<T>> supplier) {
		return supplier.get();
	}

}
