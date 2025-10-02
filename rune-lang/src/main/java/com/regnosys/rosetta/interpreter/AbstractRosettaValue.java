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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

public abstract class AbstractRosettaValue<T> implements RosettaValue {
	private List<T> items;
	
	public AbstractRosettaValue(List<T> items) {
		Validate.noNullElements(items);
		this.items = items;
	}

	@Override
	public List<T> getItems() {
		return items;
	}
	@Override
	public <U> List<U> getItems(Class<U> clazz) {
		if (items.stream().anyMatch(i -> !clazz.isInstance(i))) {
			throw new RosettaInterpreterTypeException(items + " is not of type " + clazz);
		}
		return items.stream().map(i -> clazz.cast(i)).collect(Collectors.toList());
	}
	@Override
	public RosettaValueWithNaturalOrder<?> withNaturalOrderOrThrow() {
		throw new RosettaInterpreterTypeException("Rosetta value " + this + " does not have a natural order.");
	}
	
	@Override
	public int size() {
		return items.size();
	}
	@Override
	public Stream<T> stream() {
		return items.stream();
	}
	
	@Override
	public Optional<T> getSingle() {
		if (items.size() == 1) {
			return Optional.of(items.get(0));
		}
		return Optional.empty();
	}
	@Override
	public T getSingleOrThrow() {
		return getSingle().orElseThrow(() -> new RosettaInterpreterTypeException("Expected a single item, but got " + this + "."));
	}
	@Override
	public <U> Optional<U> getSingle(Class<U> clazz) {
		return getSingle()
				.filter(v -> clazz.isInstance(v))
				.map(v -> clazz.cast(v));
	}
	@Override
	public <U> U getSingleOrThrow(Class<U> clazz) {
		Object v = getSingleOrThrow();
		if (clazz.isInstance(v)) {
			return clazz.cast(v);
		}
		throw new RosettaInterpreterTypeException("Expected a single item of type " + clazz.getSimpleName() + ", but got " + this + ".");
	}
	
	@Override
	public String toString() {
		return "[" 
				+ items.stream().map(i -> i.toString()).collect(Collectors.joining(", "))
				+ "]";
	}
	
	@Override
	public int hashCode() {
		return items.hashCode();
	}
	@Override
	public boolean equals(Object object) {
        if (!(object instanceof RosettaValue)) return false;
        
        RosettaValue other = (RosettaValue)object;
        return Objects.equals(items, other.getItems());
	}
}
