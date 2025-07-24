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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.rosetta.model.lib.expression.Converter;
import com.rosetta.model.lib.meta.FieldWithMeta;

public interface MapperBuilder<T> extends Mapper<T> {

	/**
	 * Helper function to map a value with single cardinality
	 */
	<F> MapperBuilder<F> map(String name, Function<T, F> mappingFunc);

	/**
	 * Map a value with single cardinality
	 */
	<F> MapperBuilder<F> map(NamedFunction<T, F> mappingFunc);

	/**
	 * Helper function to map a value with multiple cardinality
	 */
	<F> MapperBuilder<F> mapC(String name, Function<T, List<? extends F>> mappingFunc);
	
	/**
	 * Map a value with multiple cardinality
	 */
	<F> MapperBuilder<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc);
	
	default <C> MapperBuilder<C> convert(Class<C> clazz) {
		return map("Convert to "+clazz.getSimpleName(), i->Converter.convert(clazz, i));
	}
	
	MapperBuilder<T> unionSame(MapperBuilder<T> other);
	
	MapperBuilder<Object> unionDifferent(MapperBuilder<?> mapper);

	Stream<MapperItem<? extends T, ?>> getItems();
	
	default Optional<MapperItem<?, ?>> findParent(MapperItem<?, ?> item) {
		Optional<? extends MapperItem<?, ?>> parentItem = item.getParentItem();
		if (parentItem.isPresent()) {
			if (parentItem.get().getMappedObject() instanceof FieldWithMeta) {
				return findParent(parentItem.get());
			}
			return Optional.of(parentItem.get());
		}
		return Optional.empty();
	}
}
