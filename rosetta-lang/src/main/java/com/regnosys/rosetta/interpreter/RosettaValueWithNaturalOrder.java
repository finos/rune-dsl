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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class RosettaValueWithNaturalOrder<T extends Comparable<? super T>> extends AbstractRosettaValue<T> {	
	private final Class<T> clazz;
	
	public RosettaValueWithNaturalOrder(List<T> items, Class<T> clazz) {
		super(items);
		Objects.requireNonNull(clazz);
		this.clazz = clazz;
	}
	
	@Override
	public RosettaValueWithNaturalOrder<T> withNaturalOrderOrThrow() {
		return this;
	}
	
	public Optional<Comparable<Object>> getSingleComparable() {
		return getSingle().map(i -> new Comparable<Object>() {
				@Override
				public int compareTo(Object o) {
					if (!clazz.isInstance(o)) {
						throw new RosettaInterpreterTypeException("Cannot compare an item of type " + this.getClass().getSimpleName() + "to an item of type " + o.getClass().getSimpleName() + ".");
					}
					return i.compareTo(clazz.cast(o));
				}
			});
	}
	public Comparable<Object> getSingleComparableOrThrow() {
		T i = getSingleOrThrow();
		return new Comparable<Object>() {
			@Override
			public int compareTo(Object o) {
				if (!clazz.isInstance(o)) {
					throw new RosettaInterpreterTypeException("Cannot compare an item of type " + this.getClass().getSimpleName() + "to an item of type " + o.getClass().getSimpleName() + ".");
				}
				return i.compareTo(clazz.cast(o));
			}
		};
	}
}
