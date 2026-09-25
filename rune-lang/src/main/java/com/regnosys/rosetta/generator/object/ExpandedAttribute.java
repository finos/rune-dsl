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

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.TypeCall;

@Deprecated
public final class ExpandedAttribute {
	private final String name;
	private final String enclosingType;
	private final ExpandedType type;
	private final TypeCall rosettaType; // used in translator only
	private final boolean overriding;
	private final int inf;
	private final int sup;
	private final boolean isUnbound;
	private final List<ExpandedSynonym> synonyms;
	private final String definition;
	private final List<RosettaDocReference> docReferences;
	private final boolean isEnum;
	private final List<ExpandedAttribute> metas;

	public ExpandedAttribute(String name, String enclosingType, ExpandedType type, TypeCall rosettaType, boolean overriding, int inf, int sup, boolean isUnbound, List<ExpandedSynonym> synonyms, String definition, List<RosettaDocReference> docReferences, boolean isEnum, List<ExpandedAttribute> metas) {
		this.name = name;
		this.enclosingType = enclosingType;
		this.type = type;
		this.rosettaType = rosettaType;
		this.overriding = overriding;
		this.inf = inf;
		this.sup = sup;
		this.isUnbound = isUnbound;
		this.synonyms = synonyms;
		this.definition = definition;
		this.docReferences = docReferences;
		this.isEnum = isEnum;
		this.metas = metas;
	}

	public boolean isMultiple() {
		return isUnbound || inf > 1 || sup != 1; // sup of 0 is counted as multiple
	}

	public boolean isSingleOptional() {
		return inf == 0 && !isMultiple();
	}

	public int refIndex() {
		for (int i = 0; i < metas.size(); i++) {
			String metaName = metas.get(i).getName();
			if ("reference".equals(metaName) || "address".equals(metaName)) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasMetas() {
		return !metas.isEmpty();
	}

	public boolean hasIdAnnotation() {
		return metas.stream().anyMatch(meta -> "id".equals(meta.getName()));
	}

	public boolean isDataType() {
		return type.isType();
	}

	public boolean builtInType() {
		return type.isBuiltInType();
	}

	public String javaAnnotation() {
		if ("key".equals(name) && "Key".equals(type.getName()) && "com.rosetta.model.lib.meta".equals(type.getModel().getName())) {
			return "location";
		} else if ("reference".equals(name) && "Reference".equals(type.getName()) && "com.rosetta.model.lib.meta".equals(type.getModel().getName())) {
			return "address";
		}
		return name;
	}

	public String getName() {
		return name;
	}

	public String getEnclosingType() {
		return enclosingType;
	}

	public ExpandedType getType() {
		return type;
	}

	public TypeCall getRosettaType() {
		return rosettaType;
	}

	public boolean isOverriding() {
		return overriding;
	}

	public int getInf() {
		return inf;
	}

	public int getSup() {
		return sup;
	}

	public boolean isUnbound() {
		return isUnbound;
	}

	public List<ExpandedSynonym> getSynonyms() {
		return synonyms;
	}

	public String getDefinition() {
		return definition;
	}

	public List<RosettaDocReference> getDocReferences() {
		return docReferences;
	}

	public boolean isEnum() {
		return isEnum;
	}

	public List<ExpandedAttribute> getMetas() {
		return metas;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, enclosingType, type, rosettaType, overriding, inf, sup, isUnbound, synonyms, definition, docReferences, isEnum, metas);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof ExpandedAttribute other
				&& Objects.equals(name, other.name)
				&& Objects.equals(enclosingType, other.enclosingType)
				&& Objects.equals(type, other.type)
				&& Objects.equals(rosettaType, other.rosettaType)
				&& overriding == other.overriding
				&& inf == other.inf
				&& sup == other.sup
				&& isUnbound == other.isUnbound
				&& Objects.equals(synonyms, other.synonyms)
				&& Objects.equals(definition, other.definition)
				&& Objects.equals(docReferences, other.docReferences)
				&& isEnum == other.isEnum
				&& Objects.equals(metas, other.metas);
	}

	@Override
	public String toString() {
		return "ExpandedAttribute [name=" + name + ", enclosingType=" + enclosingType + ", type=" + type + ", inf=" + inf + ", sup=" + sup + ", isUnbound=" + isUnbound + ", isEnum=" + isEnum + ", metas=" + metas + "]";
	}
}
