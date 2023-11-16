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
