/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.mapper;

import java.util.function.Supplier;
import com.rosetta.model.lib.expression.ComparisonResult;

public class MapperUtils {
	
	/**
	 * Used when generating code for nested if statements
	 */
	public static <T> Mapper<T> runSingle(Supplier<Mapper<T>> supplier) {
		Mapper<T> result = supplier.get();
		if (result == null) {
			return MapperS.ofNull();
		}
		if (result instanceof MapperS<?> || result instanceof ComparisonResult) {
			return result;
		}
		return MapperS.of(result.get());
	}
	// Note: below method is only necessary for JavaC, as the Eclipse compiler manages to solve type boundaries just fine.
	public static <T> Mapper<? extends T> runSinglePolymorphic(Supplier<Mapper<? extends T>> supplier) {
		Mapper<? extends T> result = supplier.get();
		if (result == null) {
			return MapperS.ofNull();
		}
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
		if (result == null) {
			return MapperC.ofNull();
		}
		if (result instanceof MapperC<?>) {
			return (MapperC<T>) result;
		}
		return MapperC.of(result.getMulti());
	}
	// Note: below method is only necessary for JavaC, as the Eclipse compiler manages to solve type boundaries just fine.
	public static <T> MapperC<? extends T> runMultiPolymorphic(Supplier<Mapper<? extends T>> supplier) {
		Mapper<? extends T> result = supplier.get();
		if (result == null) {
			return MapperC.ofNull();
		}
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
		} else if (mapper.getMulti().isEmpty()) {
			return ComparisonResult.success();
		} else {
			return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
		}
	}
}