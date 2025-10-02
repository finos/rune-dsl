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
