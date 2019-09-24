package com.rosetta.model.lib.functions;

import static com.rosetta.model.lib.functions.MapperItem.getMapperItem;
import static com.rosetta.model.lib.functions.MapperItem.getMapperItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapperC<T> implements MapperBuilder<T> {
	
	private final List<MapperItem<T,?>> items;
	
	protected MapperC(List<MapperItem<T,?>> items) {
		this.items = items;
	}
	
	@SafeVarargs
	public static <T> MapperBuilder<T> of(MapperBuilder<T>... ts) {
		List<MapperItem<T, ?>> items = new ArrayList<>();
		for (MapperBuilder<T> ele : ts) {
			if (ele != null) {
				ele.getItems().forEach(item -> items.add(item));
			}
		}
		return new MapperC<T>(items);
	}
	
	/**
	 * Maps list parent item to single child item.
	 */
	@Override
	public <F> MapperBuilder<F> map(NamedFunction<T, F> mappingFunc) {
		List<MapperItem<F,?>> results = new ArrayList<>();
		
		for (int i=0; i<items.size(); i++) {
			results.add(getMapperItem(items.get(i), mappingFunc));
		}
		return new MapperC<>(results);
	}
	
	/**
	 * Maps list parent item to list child item.
	 */
	@Override
	public <F> MapperBuilder<F> mapC(NamedFunction<T, List<F>> mappingFunc) {
		List<MapperItem<F,?>> results = new ArrayList<>();
		
		for (int i=0; i<items.size(); i++) {
			results.addAll(getMapperItems(items.get(i), mappingFunc));
		}
		return new MapperC<>(results);
	}
	
	
	
	protected Stream<MapperItem<T,?>> nonErrorItems() {
		return items.stream().filter(i->!i.isError());
	}

	private Stream<MapperItem<T,?>> errorItems() {
		return items.stream().filter(MapperItem::isError);
	}
	
	@Override
	public T get() {
		List<T> collect = nonErrorItems()
				.map(i->i.getMappedObject())
				.collect(Collectors.toList());
		return collect.size()!=1 ? null : collect.get(0);
	}
	
	@Override
	public List<T> getMulti() {
		return nonErrorItems()
				.map(i->i.getMappedObject())
				.collect(Collectors.toList());
	}

	@Override
	public Optional<?> getParent() {
		List<?> collect = nonErrorItems()
			.map(i->i.getParent())
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
		return collect.size()==1 ? Optional.of(collect.get(0)) : Optional.empty();
	}
	
	@Override
	public List<?> getParentMulti() {
		return nonErrorItems()
				.map(i->i.getParent())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}
	
	@Override
	public int resultCount() {
		return (int) nonErrorItems().count();
	}

	@Override
	public List<Path> getPaths() {
		return nonErrorItems()
				.map(MapperItem::getPath)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<Path> getErrorPaths() {
		return errorItems()
				.map(MapperItem::getPath)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getErrors() {
		return errorItems()
				.map(MapperItem::getPath)
				.map(p -> String.format("[%s] is not set", p.getFullPath()))
				.collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return String.join(",", items.stream().map(i -> i.getPath().getFullPath()).collect(Collectors.toList()));
	}
	
	@Override
	public MapperBuilder<T> unionSame(MapperBuilder<T> other) {
		if(other instanceof MapperC) {
			MapperC<T> otherMapperC = (MapperC<T>) other;
			List<MapperItem<T,?>> unionItems = new ArrayList<>();
			unionItems.addAll(this.items);
			unionItems.addAll(otherMapperC.items);
			return new MapperC<>(unionItems);
		}
		else if(other instanceof MapperS) {
			return other.unionSame(this);
		}
		else {
			throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
		}
	}
	
	@Override
	public MapperBuilder<Object> unionDifferent(MapperBuilder<?> other) {
		if(other instanceof MapperC) {
			MapperC<?> otherMapperC = (MapperC<?>) other;
			List<MapperItem<Object,?>> unionItems = new ArrayList<>();
			unionItems.addAll(upcast(this));
			unionItems.addAll(upcast(otherMapperC));
			return new MapperC<>(unionItems);
		}
		else if(other instanceof MapperS) {
			return other.unionDifferent(this);
		}
		else {
			throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
		}
	}

	private List<MapperItem<Object,?>> upcast(MapperC<?> mapper) {
		return mapper.items.stream().map(MapperItem::upcast).collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
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
		MapperC<?> other = (MapperC<?>) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

	@Override
	public Stream<MapperItem<T, ?>> getItems() {
		return items.stream();
	}
}
