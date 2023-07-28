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
	// Note: below method is only necessary for JavaC, as the Eclipse compiler manages to solve type boundaries just fine.
	public static <T> Mapper<? extends T> runSinglePolymorphic(Supplier<Mapper<? extends T>> supplier) {
		Mapper<? extends T> result = supplier.get();
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
	// Note: below method is only necessary for JavaC, as the Eclipse compiler manages to solve type boundaries just fine.
	public static <T> MapperC<? extends T> runMultiPolymorphic(Supplier<Mapper<? extends T>> supplier) {
		Mapper<? extends T> result = supplier.get();
		if (result instanceof MapperC<?>) {
			return (MapperC<? extends T>) result;
		}
		return MapperC.of(result.getMulti());
	}
	
	/**
	 * Used when generating code for nested if statements
	 */
	@Deprecated
	public static <T> Mapper<T> fromBuiltInType(Supplier<Mapper<T>> supplier) {
		return supplier.get();
	}

	/**
	 * Used when generating code for nested if statements
	 */
	@Deprecated
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