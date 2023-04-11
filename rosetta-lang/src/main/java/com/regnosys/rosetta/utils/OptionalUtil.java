package com.regnosys.rosetta.utils;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class OptionalUtil {

	public static <T> Optional<T> typedGet(Map<?, ?> map, Object key, Class<T> clazz) {
		Object value = map.get(key);
		if (clazz.isInstance(value)) {
			return Optional.of(clazz.cast(value));
		}
		return Optional.empty();
	}
	
	public static <T, U> Optional<U> zipWith(Optional<T> a, Optional<T> b, BiFunction<T, T, U> zipFunc) {
		return a.flatMap(va -> b.map(vb -> zipFunc.apply(va, vb)));
	}
}
