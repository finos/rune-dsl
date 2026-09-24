/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.generator.object;

import java.util.Objects;

@Deprecated
public final class ExpandedSynonymValue {
	private final String name;
	private final String path;
	private final int maps;
	private final boolean isMeta;

	public ExpandedSynonymValue(String name, String path, int maps, boolean isMeta) {
		this.name = name;
		this.path = path;
		this.maps = maps;
		this.isMeta = isMeta;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public int getMaps() {
		return maps;
	}

	public boolean isMeta() {
		return isMeta;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, path, maps, isMeta);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof ExpandedSynonymValue other
				&& Objects.equals(name, other.name)
				&& Objects.equals(path, other.path)
				&& maps == other.maps
				&& isMeta == other.isMeta;
	}

	@Override
	public String toString() {
		return "ExpandedSynonymValue [name=" + name + ", path=" + path + ", maps=" + maps + ", isMeta=" + isMeta + "]";
	}
}
