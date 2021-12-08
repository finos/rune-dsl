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
