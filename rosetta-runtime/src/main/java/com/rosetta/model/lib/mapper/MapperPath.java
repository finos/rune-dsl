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
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;


class MapperPath implements Mapper.Path, Comparable<MapperPath> {

	private final List<PathElement> pathElements;
	
	public MapperPath(MapperPathBuilder builder) {
		this.pathElements = builder.pathElements;
	}
	
	@Override
	public List<String> getNames() {
		return Collections.unmodifiableList(pathElements.stream()
				.map(PathElement::getName)
				.collect(Collectors.toList()));
	}
	
	@Override
	public List<String> getGetters() {
		return Collections.unmodifiableList(pathElements.stream()
				.map(PathElement::getGetter)
				.collect(Collectors.toList()));
	}
	
	@Override
	public String getLastName() {
		return getNames().get(getNames().size() - 1);
	}
	
	@Override
	public String getFullPath() {
		return String.join("->", pathElements.stream()
				.map(PathElement::getGetterAndContext)
				.collect(Collectors.toList()));
	}
	
	@Override
	public String toString() {
		return getFullPath();
	}

	public static MapperPathBuilder builder() {
		return new MapperPathBuilder();
	}
	
	public MapperPathBuilder toBuilder() {
		return new MapperPathBuilder(pathElements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathElements == null) ? 0 : pathElements.hashCode());
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
		MapperPath other = (MapperPath) obj;
		if (pathElements == null) {
			if (other.pathElements != null)
				return false;
		} else if (!pathElements.equals(other.pathElements))
			return false;
		return true;
	}

	@Override
	public int compareTo(MapperPath other) {
		ListIterator<PathElement> i1 = pathElements.listIterator();
		ListIterator<PathElement> i2 = other.pathElements.listIterator();
		
		while (i1.hasNext() && i2.hasNext()) {
			int result = i1.next().compareTo(i2.next());
			if(result != 0) {
				return result;
			}
		}
		if(!i1.hasNext() && !i2.hasNext()) {
			return 0;
		}
		else if(i2.hasNext()) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	public static class MapperPathBuilder {
		
		private final List<PathElement> pathElements;
		
		public MapperPathBuilder() {
			this.pathElements = new ArrayList<>();
		}
		
		public MapperPathBuilder(List<PathElement> pathElements) {
			this.pathElements = new ArrayList<>(pathElements);
		}
		
		public MapperPath addNull() {
			pathElements.add(new PathElement("Null"));
			return new MapperPath(this);
		}
		
		public MapperPath addRoot(Class<?> clazz) {
			String name = clazz.getSimpleName();
			pathElements.add(new PathElement(name));
			return new MapperPath(this);
		}
		
		public MapperPath addFunctionName(String name) {
			pathElements.add(new PathElement(name));
			return new MapperPath(this);
		}
		
		public MapperPath addListFunctionName(String name, int listIndex) {
			pathElements.add(new PathElement(name, listIndex));
			return new MapperPath(this);
		}
	}
	
	private static class PathElement implements Comparable<PathElement> {
		
		private final String name;
		private final String getter;
		private final Optional<Integer> listIndex;
		// E.g. getFoo[1]
		private final String getterAndContext;
		
		public PathElement(String getter) {
			this(toAttributeName(getter), getter, Optional.empty());
		}

		public PathElement(String getter, int listIndex) {
			this(toAttributeName(getter), getter, Optional.of(listIndex));
		}
		
		private PathElement(String name, String getter, Optional<Integer> listIndex) {
			this.name = name;
			this.getter = getter;
			this.listIndex = listIndex;
			this.getterAndContext = listIndex
					.map(i -> String.format("%s[%s]", getter, i))
					.orElse(getter);
		}
		
		public String getName() {
			return name;
		}

		public String getGetter() {
			return getter;
		}
		
		public String getGetterAndContext() {
			return getterAndContext;
		}
		
		private static String toAttributeName(String getter) {
			if(getter.startsWith("get")) {
				return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getter.substring(3));
			}
			return getter;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getter == null) ? 0 : getter.hashCode());
			result = prime * result + ((listIndex == null) ? 0 : listIndex.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			PathElement other = (PathElement) obj;
			if (getter == null) {
				if (other.getter != null)
					return false;
			} else if (!getter.equals(other.getter))
				return false;
			if (listIndex == null) {
				if (other.listIndex != null)
					return false;
			} else if (!listIndex.equals(other.listIndex))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public int compareTo(PathElement other) {
			int nameCompare = name.compareTo(other.name);
			if (nameCompare != 0) {
		        return nameCompare;
			} else if (listIndex.isPresent() && other.listIndex.isPresent()) {
	            return listIndex.get().compareTo(other.listIndex.get());
	        } else if (listIndex.isPresent()) {
	            return -1;
	        } else if (other.listIndex.isPresent()) {
	            return 1;
	        } else {
	            return 0;
	        }
		}
	}
}
