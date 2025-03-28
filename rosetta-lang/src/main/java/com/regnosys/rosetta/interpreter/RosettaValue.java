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

package com.regnosys.rosetta.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface RosettaValue {
	List<?> getItems();
	<U> List<U> getItems(Class<U> clazz);
	RosettaValueWithNaturalOrder<?> withNaturalOrderOrThrow();
	
	int size();
	Stream<?> stream();
	
	Optional<?> getSingle();
	Object getSingleOrThrow();
	<U> Optional<U> getSingle(Class<U> clazz);
	<U> U getSingleOrThrow(Class<U> clazz);
	
	
	public static RosettaValue empty() {
		return new RosettaValueWithNaturalOrder</*Dummy class*/Integer>(Collections.emptyList(), Integer.class) {
			@Override
			public String toString() {
				return "empty";
			}
		};
	}
}
