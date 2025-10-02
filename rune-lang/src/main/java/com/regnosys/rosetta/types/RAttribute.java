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

package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;

public class RAttribute implements RAssignedRoot, RFeature {
	private final RDataType enclosingType;
	private final boolean isOverride;
	private final String name;
	private final String definition;
	private final List<RosettaDocReference> docReferences;
	private final RMetaAnnotatedType rMetaAnnotatedType;
	private final RCardinality cardinality;
	private final List<RuleReferenceAnnotation> ruleReferences;
	private final List<LabelAnnotation> labelAnnotations;
	private final Attribute origin;
	
	private RAttribute parentAttribute = null;

	public RAttribute(RDataType enclosingType, boolean isOverride, String name, String definition, List<RosettaDocReference> docReferences,
			RMetaAnnotatedType rMetaAnnotatedType, RCardinality cardinality,
			List<RuleReferenceAnnotation> ruleReferences, List<LabelAnnotation> labelAnnotations, Attribute origin) {
		this.enclosingType = enclosingType;
		this.isOverride = isOverride;
		this.name = name;
		this.definition = definition;
		this.docReferences = docReferences;
		this.rMetaAnnotatedType = rMetaAnnotatedType;
		this.cardinality = cardinality;
		this.ruleReferences = ruleReferences;
		this.labelAnnotations = labelAnnotations;
		this.origin = origin;
	}
	
	public RDataType getEnclosingType() {
		return enclosingType;
	}
	
	public boolean isOverride() {
		return isOverride;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public Attribute getEObject() {
		return origin;
	}

	public RMetaAnnotatedType getRMetaAnnotatedType() {
		return rMetaAnnotatedType;
	}

	@Override
	public boolean isMulti() {
		return cardinality.isMulti();
	}

	public RCardinality getCardinality() {
		return cardinality;
	}

	public String getDefinition() {
		return definition;
	}

	public List<RosettaDocReference> getOwnDocReferences() {
		return docReferences;
	}
	public List<RosettaDocReference> getAllDocReferences() {
		return inheritAnnotationsFromParent(RAttribute::getAllDocReferences, docReferences);
	}

	public List<RuleReferenceAnnotation> getOwnRuleReferences() {
		return ruleReferences;
	}
	public List<RuleReferenceAnnotation> getAllRuleReferences() {
		return inheritAnnotationsFromParent(RAttribute::getAllRuleReferences, ruleReferences);
	}
	
	public List<LabelAnnotation> getOwnLabelAnnotations() {
		return labelAnnotations;
	}
	public List<LabelAnnotation> getAllLabelAnnotations() {
		return inheritAnnotationsFromParent(RAttribute::getAllLabelAnnotations, labelAnnotations);
	}
	
	private <T> List<T> inheritAnnotationsFromParent(Function<RAttribute, List<T>> getter, List<T> ownAnnotations) {
		RAttribute p = getParentAttribute();
		List<T> parentAnnotations;
		if (p == null || (parentAnnotations = getter.apply(p)).isEmpty()) {
			return ownAnnotations;
		}
		List<T> allAnnotations = new ArrayList<>(ownAnnotations.size() + parentAnnotations.size());
		allAnnotations.addAll(parentAnnotations);
		allAnnotations.addAll(ownAnnotations);
		return allAnnotations;
	}
	
	public RAttribute getParentAttribute() {
		if (parentAttribute == null && isOverride && enclosingType != null) {
			RDataType currentEnclosingType = enclosingType.getSuperType();
			while (currentEnclosingType != null) {
				RAttribute foundParentAttr = currentEnclosingType.getOwnAttributeByName(name);
				if (foundParentAttr != null) {
					parentAttribute = foundParentAttr;
					break;
				}
				currentEnclosingType = currentEnclosingType.getSuperType();
			}
		}
		return parentAttribute;
	}

	@Override
	public int hashCode() {
		return Objects.hash(definition, cardinality, name, rMetaAnnotatedType, origin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RAttribute other = (RAttribute) obj;
		return Objects.equals(definition, other.definition) && Objects.equals(cardinality, other.cardinality)
				&& Objects.equals(name, other.name) && Objects.equals(rMetaAnnotatedType, other.rMetaAnnotatedType)
				&& Objects.equals(origin, other.origin);
	}

	@Override
	public String toString() {
		return String.format("RAttribute[name=%s, type=%s, cardinality=%s]", name, rMetaAnnotatedType, cardinality);
	}
}
