package com.rosetta.model.lib.mapper;

import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;

import com.rosetta.model.lib.RosettaModelObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class MapperS<T> implements MapperBuilder<T> {

	private final MapperItem<T,?> item;
	
	public MapperS(MapperItem<T,?> item) {
		this.item = item;
	}
	
	public static <T> MapperS<T> ofNull() {
		return new MapperS<>(new MapperItem<>(null, MapperPath.builder().addNull(), true, Optional.empty()));
	}

	public static <T> MapperBuilder<T> of(T t) {
		if (t==null) {
			return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addNull(), true, Optional.empty()));
		}
		if (t instanceof RosettaModelObject) {
			return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(((RosettaModelObject)t).getType()), false, Optional.empty()));
		}
		return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(t.getClass()), false, Optional.empty()));
	}
	
	public static <T,P> MapperBuilder<T> of(T t, MapperPath path, P parent) {
		if (t==null) { 
			return new MapperS<>(new MapperItem<>(t, path, true, Optional.ofNullable(parent)));
		}
		return new MapperS<>(new MapperItem<>(t, path, false, Optional.ofNullable(parent)));
	}
	
	/**
	 * Maps single parent item to single child item.
	 */
	@Override
	public <F> MapperBuilder<F> map(NamedFunction<T, F> mappingFunc) {
		return new MapperS<>(getMapperItem(item, mappingFunc));
	}
	
	/**
	 * Maps single parent item to list child item.
	 */
	@Override
	public <F> MapperBuilder<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc) {
		return new MapperC<>(getMapperItems(item, mappingFunc));
	}

	@Override
	public T get() {
		return item.getMappedObject();
	}
	
	@Override
	public List<T> getMulti() {
		return Optional.ofNullable(get())
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}
	
	@Override
	public Optional<?> getParent() {
		return item.getParent();
	}

	@Override
	public List<?> getParentMulti() {
		return getParent()
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	@Override
	public int resultCount() {
		return item.getMappedObject()!=null?1:0;
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
	public MapperBuilder<T> unionSame(MapperBuilder<T> other) {
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
	public MapperBuilder<Object> unionDifferent(MapperBuilder<?> other) {
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