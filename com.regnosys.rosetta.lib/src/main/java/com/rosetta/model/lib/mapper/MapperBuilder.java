package com.rosetta.model.lib.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rosetta.model.lib.expression.Converter;
import com.rosetta.model.lib.meta.FieldWithMeta;

public interface MapperBuilder<T> extends Mapper<T> {

	/**
	 * Helper function to map a value with single cardinality
	 */
	default <F> MapperBuilder<F> map(String name, Function<T, F> mappingFunc) {
		return map(new NamedFunctionImpl<>(name, mappingFunc));
	}

	/**
	 * Map a value with single cardinality
	 */
	<F> MapperBuilder<F> map(NamedFunction<T, F> mappingFunc);

	/**
	 * Helper function to map a value with multiple cardinality
	 */
	default <F> MapperBuilder<F> mapC(String name, Function<T, List<? extends F>> mappingFunc) {
		return mapC(new NamedFunctionImpl<T, List<? extends F>>(name, mappingFunc));
	}
	
	/**
	 * Map a value with multiple cardinality
	 */
	<F> MapperBuilder<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc);
	
	
	default <G> MapperGroupByBuilder<T,G> groupBy(Function<MapperItem<T,?>, MapperBuilder<G>> groupByFunc) {
		Function<MapperItem<T,?>,MapperItem<G, ?>> keyFunction = 
				i -> groupByFunc.apply(i).getItems().findFirst().get();
		Function<MapperItem<T,?>,MapperBuilder<T>> identity = MapperS::new;
		BinaryOperator<MapperBuilder<T>> merger = MapperBuilder<T>::unionSame;
		Map<MapperItem<G, ?>, MapperBuilder<T>> gbi = getItems()
				.collect(Collectors.toMap(keyFunction, 
					identity, 
					merger));
		return new MapperGroupByC<>(gbi);
	}

	default <C> MapperBuilder<C> convert(Class<C> clazz) {
		return map("Convert to "+clazz.getSimpleName(), i->Converter.convert(clazz, i));
	}
	
	MapperBuilder<T> unionSame(MapperBuilder<T> other);
	
	MapperBuilder<Object> unionDifferent(MapperBuilder<?> mapper);

	Stream<MapperItem<T, ?>> getItems();
	
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
