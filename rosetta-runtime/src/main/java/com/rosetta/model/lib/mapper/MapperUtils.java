package com.rosetta.model.lib.mapper;

import java.util.function.Supplier;
import com.rosetta.model.lib.expression.ComparisonResult;

public class MapperUtils {
	
	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> Mapper<T> fromBuiltInType(Supplier<Mapper<T>> supplier) {
		return supplier.get();
	}

	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> Mapper<? extends T> fromDataType(Supplier<Mapper<? extends T>> supplier) {
		return supplier.get();
	}

	public static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
		if (mapper instanceof ComparisonResult) {
			return (ComparisonResult) mapper;
		} else {
			return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
		}
	}
}