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

import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
import static com.rosetta.model.lib.mapper.MapperItem.getCheckedMapperItem;
import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.rosetta.model.lib.RosettaModelObject;


public class MapperS<T> implements MapperBuilder<T> {

	private final MapperItem<? extends T,?> item;
	private final boolean identity;
	
	public MapperS(MapperItem<? extends T,?> item) {
		this(item, false);
	}
	
	public MapperS(MapperItem<? extends T,?> item, boolean identity) {
		this.item = item;
		this.identity = identity;
	}
	
	public static <T> MapperS<T> identity() {
		return new MapperS<>(new MapperItem<>(null, MapperPath.builder().addNull(), true, Optional.empty()), true);
	}
	
	public static <T> MapperS<T> ofNull() {
		return new MapperS<>(new MapperItem<>(null, MapperPath.builder().addNull(), true, Optional.empty()));
	}

	public static <T> MapperS<T> of(T t) {
		if (t==null) {
			return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addNull(), true, Optional.empty()));
		}
		if (t instanceof RosettaModelObject) {
			return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(((RosettaModelObject)t).getType()), false, Optional.empty()));
		}
		return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(t.getClass()), false, Optional.empty()));
	}
	
	public static <T,P> MapperS<T> of(T t, MapperPath path, MapperItem<P, ?> parent) {
		if (t==null) { 
			return new MapperS<>(new MapperItem<>(t, path, true, Optional.ofNullable(parent)));
		}
		return new MapperS<>(new MapperItem<>(t, path, false, Optional.ofNullable(parent)));
	}
	
	public MapperS<T> filterSingle(Predicate<MapperS<T>> predicate) {
		if (!item.isError()) {
			boolean condition = predicate.test(this);
			if (condition) {
				return this;
			} else {
				return ofNull();
			}
		}
		else {
			return this;
		}
}
	public MapperS<T> filterSingleNullSafe(Function<MapperS<T>, Boolean> predicate) {
		if (!item.isError()) {
			Boolean condition = predicate.apply(this);
			if (condition != null && condition) {
				return this;
			} else {
				return ofNull();
			}
		}
		else {
			return this;
		}
	}
	
	@Override
	public <F> MapperS<F> map(String name, Function<T, F> mappingFunc) {
		return map(new NamedFunctionImpl<>(name, mappingFunc));
	}
	
	/**
	 * Maps single parent item to single child item.
	 */
	@Override
	public <F> MapperS<F> map(NamedFunction<T, F> mappingFunc) {
		return new MapperS<>(getMapperItem(item, mappingFunc));
	}
	
	public <F> MapperS<F> checkedMap(String name, Function<T, F> mappingFunc, Class<? extends Exception> errorClass) {
		return checkedMap(new NamedFunctionImpl<>(name, mappingFunc), errorClass);
	}
	
	public <F> MapperS<F> checkedMap(NamedFunction<T, F> mappingFunc, Class<? extends Exception> errorClass) {
		return new MapperS<>(getCheckedMapperItem(item, mappingFunc, errorClass));
	}
	
	@Override
	public <F> MapperC<F> mapC(String name, Function<T, List<? extends F>> mappingFunc) {
		return mapC(new NamedFunctionImpl<T, List<? extends F>>(name, mappingFunc));
	}
	
	/**
	 * Maps single parent item to list child item.
	 */
	@Override
	public <F> MapperC<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc) {
		return new MapperC<>(getMapperItems(item, mappingFunc));
	}
	
	@Override
	public T get() {
		return item.getMappedObject();
	}
	
	@Override
	public T getOrDefault(T defaultValue) {
		return Optional.<T>ofNullable(item.getMappedObject()).orElse(defaultValue);
	}
	
	@Override
	public List<T> getMulti() {
		return Optional.ofNullable(get())
				.map(Arrays::asList)
				.orElseGet(ArrayList::new);
	}
	
	@Override
	public Optional<?> getParent() {
		return findParent(item)
				.map(MapperItem::getMappedObject);
	}

	@Override
	public List<?> getParentMulti() {
		return findParent(item)
				.map(MapperItem::getMappedObject)
				.map(Arrays::asList)
				.orElseGet(ArrayList::new);
	}

	@Override
	public int resultCount() {
		return item.getMappedObject()!=null?1:0;
	}
	
	public boolean isIdentity() {
		return identity;
	}
	
	/**
	 * Map a single value to a single value.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperS<F> mapSingleToItem(Function<MapperS<T>, MapperS<F>> mappingFunc) {
		if (item.getMappedObject() == null) {
			return MapperS.ofNull();
		}
		return mappingFunc.apply(this);
	}
	
	/**
	 * Map a single value into an item of a list based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperC<F> mapSingleToList(Function<MapperS<T>, MapperC<F>> mappingFunc) {
		if (item.getMappedObject() == null) {
			return MapperC.ofNull();
		}
		return mappingFunc.apply(this);
	}
	
	/**
	 * Apply a function to this mapper
	 */
	public <F> F apply(Function<MapperS<T>, F> f) {
		return f.apply(this);
	}
	
	@Override
	public List<Path> getPaths() {
		return !item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
	}
	
	@Override
	public List<Path> getErrorPaths() {
		return item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
	}
	
	@Override
	public List<String> getErrors() {
		return item.isError() ? Collections.singletonList(item.getPath().toString() +" was null") : Collections.emptyList();
	}
	
	@Override
	public String toString() {
		return item.getPath().toString();
	}
	
	@Override
	public MapperC<T> unionSame(MapperBuilder<T> other) {
		if(other instanceof MapperS) {
			MapperS<T> otherMapperS = (MapperS<T>) other;
			return new MapperC<>(Arrays.asList(this.item, otherMapperS.item));
		}
		else if(other instanceof MapperC) {
			return new MapperC<T>(Collections.singletonList(this.item)).unionSame(other);
		}
		else {
			throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
		}
	}
	
	@Override
	public MapperC<Object> unionDifferent(MapperBuilder<?> other) {
		if(other instanceof MapperS) {
			MapperS<?> otherMapperS = (MapperS<?>) other;
			return new MapperC<>(Arrays.asList(this.item.upcast(), otherMapperS.item.upcast()));
		}
		else if(other instanceof MapperC) {
			return new MapperC<>(Collections.singletonList(this.item.upcast())).unionDifferent(other);
		}
		else {
			throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapperS<?> other = (MapperS<?>) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		return true;
	}

	@Override
	public Stream<MapperItem<? extends T, ?>> getItems() {
		return Stream.of(item);
	}
}