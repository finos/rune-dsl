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

package com.rosetta.model.lib.mapper;

import java.util.List;
import java.util.Optional;

public class MapperListItem<T, P> extends AbstractMapperItem<P> {
	
	private final List<T> mappedObjects;
	
	MapperListItem(List<T> mappedObjects, MapperPath path, boolean error, Optional<MapperItem<? extends P, ?>> parentItem) {
		super(path, error, parentItem);
		this.mappedObjects = mappedObjects;
	}

	// mapped value
	
	public List<T> getMappedObjects() {
		return mappedObjects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappedObjects == null) ? 0 : mappedObjects.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapperListItem<?, ?> other = (MapperListItem<?, ?>) obj;
		if (mappedObjects == null) {
			if (other.mappedObjects != null)
				return false;
		} else if (!mappedObjects.equals(other.mappedObjects))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MapperListItem [mappedObjects=" + mappedObjects + "]";
	}
}
