package com.rosetta.model.lib.mapper;

import java.util.function.Supplier;
import com.rosetta.model.lib.expression.ComparisonResult;

public class MapperUtils {
	
	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> Mapper<T> runSingle(Supplier<Mapper<T>> supplier) {
		Mapper<T> result = supplier.get();
		if (result instanceof MapperS<?> || result instanceof ComparisonResult) {
			return result;
		}
		return MapperS.of(result.get());
	}
	
	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> MapperC<T> runMulti(Supplier<Mapper<T>> supplier) {
		Mapper<T> result = supplier.get();
		if (result instanceof MapperC<?>) {
			return (MapperC<T>) result;
		}
		return MapperC.of(result.getMulti());
	}

	public static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
		if (mapper instanceof ComparisonResult) {
			return (ComparisonResult) mapper;
		} else {
			return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
		}
	}
}