package com.regnosys.rosetta.utils;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

public class OptionalUtil {

	public static <T, K> Optional<T> typedGet(Map<K, ?> map, K key, Class<T> clazz) {
		Validate.notNull(map);
		Validate.notNull(key);
		Validate.notNull(clazz);
		
		Object value = map.get(key);
		if (clazz.isInstance(value)) {
			return Optional.of(clazz.cast(value));
		}
		return Optional.empty();
	}
	
	public static <T, U> Optional<U> zipWith(Optional<T> a, Optional<T> b, BiFunction<T, T, U> zipFunc) {
		Validate.notNull(a);
		Validate.notNull(b);
		Validate.notNull(zipFunc);
		
		return a.flatMap(va -> b.map(vb -> zipFunc.apply(va, vb)));
	}
}
