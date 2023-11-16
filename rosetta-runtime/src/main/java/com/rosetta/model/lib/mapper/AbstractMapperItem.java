package com.rosetta.model.lib.mapper;

import java.util.Optional;

public abstract class AbstractMapperItem<P> implements Comparable<AbstractMapperItem<P>> {

	private final MapperPath path;
	private final boolean error;
	private final Optional<MapperItem<? extends P, ?>> parentItem;
	
	AbstractMapperItem(MapperPath path, boolean error, Optional<MapperItem<? extends P, ?>> parentItem) {
		this.path = path;
		this.error = error;
		this.parentItem = parentItem;
	}

	public MapperPath getPath() {
		return path;
	}

	public boolean isError() {
		return error;
	}
	
	// parent
	
	public Optional<MapperItem<? extends P, ?>> getParentItem() {
		return parentItem;
	}

	@Override
	public int compareTo(AbstractMapperItem<P> other) {
		return path.compareTo(other.path);
	}
}
