package com.regnosys.rosetta.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class OptionalUtil {

	public static <T, K> Optional<T> typedGet(Map<K, ?> map, K key, Class<T> clazz) {
		Objects.requireNonNull(map);
		Objects.requireNonNull(key);
		Objects.requireNonNull(clazz);
		
		Object value = map.get(key);
		if (clazz.isInstance(value)) {
			return Optional.of(clazz.cast(value));
		}
		return Optional.empty();
	}
	
	public static <T, U> Optional<U> zipWith(Optional<T> a, Optional<T> b, BiFunction<T, T, U> zipFunc) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(zipFunc);
		
		return a.flatMap(va -> b.map(vb -> zipFunc.apply(va, vb)));
	}
}
