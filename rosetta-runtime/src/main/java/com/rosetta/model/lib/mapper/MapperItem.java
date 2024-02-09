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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MapperItem<T, P> extends AbstractMapperItem<P> {

	static <C, P> MapperItem<C, P> getMapperItem(MapperItem<? extends P, ?> parentItem, NamedFunction<P, C> mappingFunc) {
		if (!parentItem.isError()) {
			P parent = parentItem.getMappedObject();
			C child = mappingFunc.apply(parent);
			MapperPath path = parentItem.getPath().toBuilder().addFunctionName(mappingFunc.getName());
			boolean error = child == null;
			return new MapperItem<>(child, path, error, Optional.of(parentItem));
		}
		else {
			return getErrorMapperItem(parentItem.getPath());
		}
	}
	
	static <C, P> MapperItem<C, P> getCheckedMapperItem(MapperItem<? extends P, ?> parentItem, NamedFunction<P, C> mappingFunc, Class<? extends Exception> errorClass) {
		if (!parentItem.isError()) {
			P parent = parentItem.getMappedObject();
			C child;
			try {
				child = mappingFunc.apply(parent);
			} catch (Exception e) {
				if (!errorClass.isInstance(e)) {
					throw e;
				}
				child = null;
			}
			MapperPath path = parentItem.getPath().toBuilder().addFunctionName(mappingFunc.getName());
			boolean error = child == null;
			return new MapperItem<>(child, path, error, Optional.of(parentItem));
		}
		else {
			return getErrorMapperItem(parentItem.getPath());
		}
	}
	
	static <C, P> List<MapperItem<? extends C, ?>> getMapperItems(MapperItem<? extends P, ?> parentItem, NamedFunction<P, List<? extends C>> mappingFunc) {
		if (!parentItem.isError()) {
			List<MapperItem<? extends C, ?>> childItems = new ArrayList<>();
			
			P parent = parentItem.getMappedObject();
			List<? extends C> children = mappingFunc.apply(parent);
			
			if (children!=null && !children.isEmpty()) {
				for (int j=0; j<children.size(); j++) {
					C child = children.get(j);
					MapperPath path = parentItem.getPath().toBuilder().addListFunctionName(mappingFunc.getName(), j);
					boolean error = child == null;
					childItems.add(new MapperItem<>(child, path, error, Optional.of(parentItem)));
				}
				return childItems;
			}
			else {
				MapperPath childPath = parentItem.getPath().toBuilder().addFunctionName(mappingFunc.getName());
				return Collections.singletonList(getErrorMapperItem(childPath));
			}
		}
		else {
			return Collections.singletonList(getErrorMapperItem(parentItem.getPath()));
		}
	}
	
	private static <C, P> MapperItem<C, P> getErrorMapperItem(MapperPath path) {
		return new MapperItem<>(null, path, true, Optional.empty());
	}
	
	private final T mappedObject;
	
	MapperItem(T mappedObject, MapperPath path, boolean error, Optional<MapperItem<? extends P, ?>> parentItem) {
		super(path, error, parentItem);
		this.mappedObject = mappedObject;
	}

	// mapped value
	
	public T getMappedObject() {
		return mappedObject;
	}

	public MapperItem<Object,?> upcast() {
		return new MapperItem<>(mappedObject, getPath(), isError(), getParentItem());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappedObject == null) ? 0 : mappedObject.hashCode());
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
		MapperItem<?, ?> other = (MapperItem<?, ?>) obj;
		if (mappedObject == null) {
			if (other.mappedObject != null)
				return false;
		} else if (!mappedObject.equals(other.mappedObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MapperItem [mappedObject=" + mappedObject + "]";
	}
}
