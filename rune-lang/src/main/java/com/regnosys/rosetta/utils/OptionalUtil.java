/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
