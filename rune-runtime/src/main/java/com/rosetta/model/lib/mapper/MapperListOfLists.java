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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapperListOfLists<T> {
	
	private final List<MapperListItem<T,?>> items;
	
	protected MapperListOfLists(List<MapperListItem<T,?>> items) {
		this.items = items;
	}
	
	public static <T> MapperListOfLists<T> of(List<List<T>> ts) {
		List<MapperListItem<T, ?>> items = new ArrayList<>();
		if (ts != null) {
			for (List<T> ele : ts) {
				if (ele != null) {
					items.add(new MapperListItem<>(ele, MapperPath.builder().addRoot(ele.getClass()), false, Optional.empty()));
				}
			}
		}
		return new MapperListOfLists<T>(items);
	}
	
	/**
	 * Filter a list of lists based on the given predicate.
	 * 
	 * @param predicate - test that determines whether to filter list item. True to include in list, and false to exclude.
	 * @return filtered list 
	 */
	public MapperListOfLists<T> filterList(Predicate<MapperC<T>> predicate) {
		return new MapperListOfLists<>(items.stream()
				.filter(item -> predicate.test(MapperC.of(item.getMappedObjects())))
				.collect(Collectors.toList()));
	}
	/**
	 * Filter a list of lists based on the given predicate.
	 * 
	 * @param predicate - test that determines whether to filter list item. True to include in list, and false to exclude.
	 * @return filtered list 
	 */
	public MapperListOfLists<T> filterListNullSafe(Function<MapperC<T>, Boolean> predicate) {
		return new MapperListOfLists<>(items.stream()
				.filter(item -> {
					Boolean result = predicate.apply(MapperC.of(item.getMappedObjects()));
					return result != null && result;
				})
				.collect(Collectors.toList()));
	}
	
	/**
	 * Map a list of lists to a list based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperC<F> mapListToItem(Function<MapperC<T>, MapperS<F>> mappingFunc) {
		return MapperC.of(items.stream()
				.map(item -> mappingFunc.apply(MapperC.of(item.getMappedObjects())))
				.map(MapperS::get)
				.collect(Collectors.toList()));
	}
	
	/**
	 * Map list of lists to a different list of lists based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperListOfLists<F> mapListToList(Function<MapperC<T>, MapperC<F>> mappingFunc) {
		return MapperListOfLists.of(items.stream()
				.map(item -> mappingFunc.apply(MapperC.of(item.getMappedObjects())))
				.map(MapperC::getMulti)
				.collect(Collectors.toList()));
	}
	
	/**
	 * Apply a function to this mapper
	 */
	public <F> F apply(Function<MapperListOfLists<T>, F> f) {
		return f.apply(this);
	}
	
	/**
	 * Flatten a list of lists to a list.
	 * 
	 * @return flattened list
	 */
	public MapperC<T> flattenList() {
		return MapperC.of(items.stream()
				.map(item -> item.getMappedObjects())
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
	}
}
