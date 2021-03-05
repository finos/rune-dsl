package com.rosetta.model.lib.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MapperItem<T, P> implements Comparable<MapperItem<?, ?>> {

	static <C, P> MapperItem<C, P> getMapperItem(MapperItem<P, ?> parentItem, NamedFunction<P, C> mappingFunc) {
		if (!parentItem.isError()) {
			P parent = parentItem.getMappedObject();
			C child = mappingFunc.apply(parent);
			MapperPath path = parentItem.getPath().toBuilder().addFunctionName(mappingFunc.getName());
			boolean error = child == null;
			return new MapperItem<>(child, path, error, Optional.of(parent));
		}
		else {
			return getErrorMapperItem(parentItem.getPath());
		}
	}
	
	static <C, P> List<MapperItem<C, ?>> getMapperItems(MapperItem<P, ?> parentItem, NamedFunction<P, List<? extends C>> mappingFunc) {
		if (!parentItem.isError()) {
			List<MapperItem<C, ?>> childItems = new ArrayList<>();
			
			P parent = parentItem.getMappedObject();
			List<? extends C> children = mappingFunc.apply(parent);
			
			if (children!=null && !children.isEmpty()) {
				for (int j=0; j<children.size(); j++) {
					C child = children.get(j);
					MapperPath path = parentItem.getPath().toBuilder().addListFunctionName(mappingFunc.getName(), j);
					boolean error = child == null;
					childItems.add(new MapperItem<>(child, path, error, Optional.of(parent)));
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
	private final MapperPath path;
	private final boolean error;
	private final Optional<P> parent;
	
	MapperItem(T mappedObject, MapperPath path, boolean error, Optional<P> parent) {
		this.mappedObject = mappedObject;
		this.path = path;
		this.error = error;
		this.parent = parent;
	}

	// mapped value
	
	public T getMappedObject() {
		return mappedObject;
	}

	public MapperPath getPath() {
		return path;
	}

	public boolean isError() {
		return error;
	}
	
	// parent
	
	public Optional<P> getParent() {
		return parent;
	}
	
	public MapperItem<Object,?> upcast() {
		return new MapperItem<>(mappedObject, path, error, parent);
	}
	
	@Override
	public String toString() {
		return "MapperItem [mappedObject=" + mappedObject + ", path=" + path + ", error=" + error + ", parent=" + parent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (error ? 1231 : 1237);
		result = prime * result + ((mappedObject == null) ? 0 : mappedObject.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		MapperItem<?,?> other = (MapperItem<?,?>) obj;
		if (error != other.error)
			return false;
		if (mappedObject == null) {
			if (other.mappedObject != null)
				return false;
		} else if (!mappedObject.equals(other.mappedObject))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public int compareTo(MapperItem<?, ?> other) {
		return path.compareTo(other.path);
	}
}
