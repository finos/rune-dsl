package com.rosetta.model.lib.mapper;

import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.rosetta.model.lib.RosettaModelObject;


public class MapperS<T> implements MapperBuilder<T> {

	private final MapperItem<T,?> item;
	private final boolean identity;
	
	public MapperS(MapperItem<T,?> item) {
		this(item, false);
	}
	
	public MapperS(MapperItem<T,?> item, boolean identity) {
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
		return Optional.ofNullable(item.getMappedObject()).orElse(defaultValue);
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
	 * Map a single value into an item of a list based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperS<F> mapSingleToItem(Function<MapperS<T>, MapperS<F>> mappingFunc) {
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
		return mappingFunc.apply(this);
	}
	
	/* (non-Javadoc)
	 * @see com.rosetta.model.lib.blueprint.Mapper#getPath()
	 */
	@Override
	public List<Path> getPaths() {
		return !item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see com.rosetta.model.lib.blueprint.Mapper#getPath()
	 */
	@Override
	public List<Path> getErrorPaths() {
		return item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see com.rosetta.model.lib.blueprint.Mapper#getError()
	 */
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
			return new MapperC<>(Collections.singletonList(this.item)).unionSame(other);
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
	public Stream<MapperItem<T, ?>> getItems() {
		return Stream.of(item);
	}
}