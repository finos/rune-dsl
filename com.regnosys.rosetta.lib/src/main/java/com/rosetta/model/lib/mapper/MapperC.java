package com.rosetta.model.lib.mapper;

import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapperC<T> implements MapperBuilder<T> {
	
	private final List<MapperItem<T,?>> items;
	
	protected MapperC(List<MapperItem<T,?>> items) {
		this.items = items;
	}
	
	public static <T> MapperC<T> ofNull() {
		return new MapperC<>(new ArrayList<>());
	}
	
	@SafeVarargs
	public static <T> MapperC<T> of(MapperBuilder<T>... ts) {
		List<MapperItem<T, ?>> items = new ArrayList<>();
		if (ts != null) {
			for (MapperBuilder<T> ele : ts) {
				if (ele != null) {
					ele.getItems().forEach(item -> items.add(item));
				}
			}
		}
		return new MapperC<T>(items);
	}

	public static <T> MapperC<T> of(List<T> ts) {
		List<MapperItem<T, ?>> items = new ArrayList<>();
		if (ts != null) {
			for (T ele : ts) {
				if (ele == null) {
					items.add(new MapperItem<>(ele, MapperPath.builder().addNull(), true, Optional.empty()));
				} else {
					items.add(new MapperItem<>(ele, MapperPath.builder().addRoot(ele.getClass()), false, Optional.empty()));
				}
			}
		}
		return new MapperC<T>(items);
	}
	
	@Override
	public <F> MapperC<F> map(String name, Function<T, F> mappingFunc) {
		return map(new NamedFunctionImpl<>(name, mappingFunc));
	}
	
	/**
	 * Maps list parent item to single child item.
	 */
	@Override
	public <F> MapperC<F> map(NamedFunction<T, F> mappingFunc) {
		List<MapperItem<F,?>> results = new ArrayList<>();
		
		for (int i=0; i<items.size(); i++) {
			results.add(getMapperItem(items.get(i), mappingFunc));
		}
		return new MapperC<>(results);
	}
	
	@Override
	public <F> MapperC<F> mapC(String name, Function<T, List<? extends F>> mappingFunc) {
		return mapC(new NamedFunctionImpl<T, List<? extends F>>(name, mappingFunc));
	}
	
	/**
	 * Maps list parent item to list child item.
	 */
	@Override
	public <F> MapperC<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc) {
		List<MapperItem<F,?>> results = new ArrayList<>();
		
		for (int i=0; i<items.size(); i++) {
			results.addAll(getMapperItems(items.get(i), mappingFunc));
		}
		return new MapperC<>(results);
	}
	
	/**
	 * Filter items of list based on the given predicate.
	 * 
	 * @param predicate - test that determines whether to filter list item. True to include in list, and false to exclude.
	 * @return filtered list 
	 */
	public MapperC<T> filterItem(Predicate<MapperS<T>> predicate) {
		return new MapperC<>(nonErrorItems()
				.filter(item -> predicate.test(new MapperS<>(item)))
				.collect(Collectors.toList()));
	}
	
	/**
	 * Map items of a list based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperC<F> mapItem(Function<MapperS<T>, MapperS<F>> mappingFunc) {
		return MapperC.of(nonErrorItems()
				.map(item -> new MapperS<>(item))
				.map(m -> mappingFunc.apply(m))
				.map(MapperS::get)
				.collect(Collectors.toList()));
	}
	
	/**
	 * Map items of a list based on the given mapping function.
	 * 
	 * @param <F>
	 * @param mappingFunc
	 * @return mapped list
	 */
	public <F> MapperListOfLists<F> mapItemToList(Function<MapperS<T>, MapperC<F>> mappingFunc) {
		return MapperListOfLists.of(nonErrorItems()
				.map(item -> new MapperS<>(item))
				.map(m -> mappingFunc.apply(m))
				.map(MapperC::getMulti)
				.collect(Collectors.toList()));
	}
	
	/**
	 * Apply a function to this mapper
	 */
	public <F> F apply(Function<MapperC<T>, F> f) {
		return f.apply(this);
	}

	/**
	 * Reduce list items to single item based on the given reduce function.
	 * 
	 * @param <F>
	 * @param reduceFunc
	 * @return reduced item
	 */
	public <F> MapperS<F> reduce(BinaryOperator<MapperS<F>> reduceFunc) {
		return reduce(MapperS.identity(), reduceFunc);
	}
	
	/**
	 * Reduce list items to single item based on the given reduce function.
	 * 
	 * @param <F>
	 * @param reduceFunc
	 * @return reduced item
	 */
	@SuppressWarnings("unchecked")
	public <F> MapperS<F> reduce(MapperS<F> initial, BinaryOperator<MapperS<F>> reduceFunc) {
		return nonErrorItems()
				.map(item -> new MapperS<>((MapperItem<F, ?>) item))
				.reduce(initial, (m1, m2) -> {
						if (m1.isIdentity())
							return m2;
						else if (m2.isIdentity())
							return m1;
						else
							return reduceFunc.apply(m1, m2);
					});
	}
	
	/**
	 * Sum list of integers.
	 * 
	 * @return total of summed integers.
	 */
	public MapperS<Integer> sumInteger() {
		return MapperS.of(nonErrorItems()
				.map(MapperItem::getMappedObject)
				.map(Integer.class::cast)
				.reduce(0, Integer::sum));
	}
	
	/**
	 * Sum list of numbers.
	 * 
	 * @return total of summed numbers.
	 */
	public MapperS<BigDecimal> sumBigDecimal() {
		return MapperS.of(nonErrorItems()
				.map(MapperItem::getMappedObject)
				.map(BigDecimal.class::cast)
				.reduce(BigDecimal.ZERO, BigDecimal::add));
	}
	
	/**
	 * Concatenate list of strings, separating each item with delimiter.
	 * 
	 * @param delimiter - item separator
	 * @return concatenated string
	 */
	public MapperS<String> join(MapperS<String> delimiter) {
		return MapperS.of(nonErrorItems()
				.map(MapperItem::getMappedObject)
				.map(String.class::cast)
				.collect(Collectors.joining(delimiter.getOrDefault(""))));
	}
	
	/**
	 * Get minimum item from a list of comparable items.
	 * 
	 * @return minimum
	 */
	@SuppressWarnings("unchecked")
	public <F extends Comparable<F>> MapperS<T> min() {
		return min(x -> (MapperS<F>) x);
	}
	
	/**
	 * Get item from list based on minimum item attribute (provided by comparableGetter)
	 * 
	 * @param <F>
	 * @param comparableGetter - getter for comparable attribute
	 * @return minimum
	 */
	public <F extends Comparable<F>> MapperS<T> min(Function<MapperS<T>, MapperS<F>> comparableGetter) {
		return nonErrorItems()
				.map(item -> new MapperS<>(item))
				.filter(item -> comparableGetter.apply(item).get() != null)
				.min(Comparator.comparing(item -> comparableGetter.apply(item).get()))
				.orElse(MapperS.ofNull());
				
	}

	/**
	 * Get maximum item from a list of comparable items.
	 * 
	 * @return maximum
	 */
	@SuppressWarnings("unchecked")
	public <F extends Comparable<F>> MapperS<T> max() {
		return max(x -> (MapperS<F>) x);
	}
	
	/**
	 * Get item from list based on maximum item attribute (provided by comparableGetter)
	 * 
	 * @param <F>
	 * @param comparableGetter - getter for comparable attribute
	 * @return maximum
	 */
	public <F extends Comparable<F>> MapperS<T> max(Function<MapperS<T>, MapperS<F>> comparableGetter) {
		return nonErrorItems()
				.map(item -> new MapperS<>(item))
				.filter(item -> comparableGetter.apply(item).get() != null)
				.max(Comparator.comparing(item -> comparableGetter.apply(item).get()))
				.orElse(MapperS.ofNull());
	}
	
	/**
	 * Sort list of comparable items.
	 * 
	 * @return sorted list
	 */
	public MapperC<T> sort() {
		return MapperC.of(nonErrorItems()
				.map(MapperItem::getMappedObject)
				.sorted()
				.collect(Collectors.toList()));
	}
	
	/**
	 * Sort list of items based on comparable attribute.
	 * 
	 * @param <F> comparable type
	 * @param comparableGetter to get comparable item to sort by
	 * @return sorted list
	 */
	public <F extends Comparable<F>> MapperC<T> sort(Function<MapperS<T>, MapperS<F>> comparableGetter) {
		return new MapperC<>(nonErrorItems()
				.sorted(Comparator.comparing(item -> comparableGetter.apply(new MapperS<>(item)).get()))
				.collect(Collectors.toList()));
	}
	
	/**
	 * Reverse items of a list.
	 * 
	 * @return reversed list
	 */
	public MapperC<T> reverse() {
		List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
		Collections.reverse(nonErrorItems);
		return new MapperC<>(nonErrorItems);
	}
	
	/**
	 * Gets first item of the list.
	 * 
	 * @return first list item
	 */
	public MapperS<T> first() {
		return nonErrorItems()
				.findFirst()
				.map(MapperS::new)
				.orElse(MapperS.ofNull());
	}
	
	/**
	 * Gets last item of the list.
	 * 
	 * @return last list item
	 */
	public MapperS<T> last() {
		return nonErrorItems()
				.reduce((first, second) -> second)
				.map(MapperS::new)
				.orElse(MapperS.ofNull());
	}
	
	/**
	 * Get item at specified index, returns null if index out of bounds.
	 * 
	 * @return list item at index
	 */
	public MapperS<T> getItem(MapperS<Integer> indexGetter) {
		List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
		Integer index = indexGetter.get();
		if (index != null && index < nonErrorItems.size()) {
			return new MapperS<>(nonErrorItems.get(index));
		}
		return MapperS.ofNull();
	}
	
	/**
	 * Remove item at specified index, returns list without removed item.
	 * 
	 * @return list without specified item
	 */
	public MapperC<T> removeItem(MapperS<Integer> indexGetter) {
		List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
		Integer index = indexGetter.get();
		if (index != null && index < nonErrorItems.size()) {
			nonErrorItems.remove(index.intValue());
		}
		return new MapperC<>(nonErrorItems);
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
	public T getOrDefault(T defaultValue) {
		return Optional.ofNullable(get()).orElse(defaultValue);
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
			.map(this::findParent)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(MapperItem::getMappedObject)
			.collect(Collectors.toList());
		return collect.size()==1 ? Optional.of(collect.get(0)) : Optional.empty();
	}
	
	@Override
	public List<?> getParentMulti() {
		return nonErrorItems()
				.map(this::findParent)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(MapperItem::getMappedObject)
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
	public MapperC<T> unionSame(MapperBuilder<T> other) {
		if(other instanceof MapperC) {
			MapperC<T> otherMapperC = (MapperC<T>) other;
			List<MapperItem<T,?>> unionItems = new ArrayList<>();
			unionItems.addAll(this.items);
			unionItems.addAll(otherMapperC.items);
			return new MapperC<>(unionItems);
		}
		else if(other instanceof MapperS) {
			return ((MapperS<T>) other).unionSame(this);
		}
		else {
			throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
		}
	}
	
	@Override
	public MapperC<Object> unionDifferent(MapperBuilder<?> other) {
		if(other instanceof MapperC) {
			MapperC<?> otherMapperC = (MapperC<?>) other;
			List<MapperItem<Object,?>> unionItems = new ArrayList<>();
			unionItems.addAll(upcast(this));
			unionItems.addAll(upcast(otherMapperC));
			return new MapperC<>(unionItems);
		}
		else if(other instanceof MapperS) {
			return ((MapperS<?>) other).unionDifferent(this);
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
