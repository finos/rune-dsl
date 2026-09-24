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

import com.regnosys.rosetta.rosetta.RosettaModel;

@Deprecated
public final class ExpandedType {
	private final RosettaModel model;
	private final String name;
	private final boolean type; // type is instance of Data
	private final boolean enumeration; // type is instance of Enumeration
	private final boolean metaType; // type is instance of RosettaMetaType

	public ExpandedType(RosettaModel model, String name, boolean type, boolean enumeration, boolean metaType) {
		this.model = model;
		this.name = name;
		this.type = type;
		this.enumeration = enumeration;
		this.metaType = metaType;
	}

	public boolean isBuiltInType() {
		return !(type || enumeration);
	}

	public RosettaModel getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public boolean isType() {
		return type;
	}

	public boolean isEnumeration() {
		return enumeration;
	}

	public boolean isMetaType() {
		return metaType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(model, name, type, enumeration, metaType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof ExpandedType other
				&& Objects.equals(model, other.model)
				&& Objects.equals(name, other.name)
				&& type == other.type
				&& enumeration == other.enumeration
				&& metaType == other.metaType;
	}

	@Override
	public String toString() {
		return "ExpandedType [name=" + name + ", type=" + type + ", enumeration=" + enumeration + ", metaType=" + metaType + "]";
	}
}
