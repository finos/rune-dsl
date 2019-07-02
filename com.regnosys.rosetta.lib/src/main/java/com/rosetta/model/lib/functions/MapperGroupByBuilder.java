package com.rosetta.model.lib.functions;

import java.util.List;
import java.util.function.Function;

public interface MapperGroupByBuilder<T,G> extends MapperGroupBy<T,G>{
	
	
	/**
	 * Helper function to map a value with single cardinality
	 */
	default <F> MapperGroupByBuilder<F,G> map(String name, Function<T,F> mappingFunc) {
		return map(new NamedFunctionImpl<>(name, mappingFunc));
	}
	
	/**
	 * Map a value with single cardinality
	 */
	<F> MapperGroupByBuilder<F,G> map(NamedFunction<T, F> mappingFunc);
	
	/**
	 * Helper function to map a value with multiple cardinality
	 */
	default <F> MapperGroupByBuilder<F,G> mapC(String name, Function<T, List<F>> mappingFunc) {
		return mapC(new NamedFunctionImpl<>(name, mappingFunc));
	}
	
	/**
	 * Map a value with multiple cardinality
	 */
	<F> MapperGroupByBuilder<F,G> mapC(NamedFunction<T, List<F>> mappingFunc);
	
	//there are 3 more methods in MapperBuilder that may or may not be required
	
}
