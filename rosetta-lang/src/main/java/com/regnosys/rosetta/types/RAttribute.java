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

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;

public class RAttribute implements RAssignedRoot {
	private final String name;
	private final String definition;
	private final List<RosettaDocReference> docReferences;
	private final RMetaAnnotatedType rMetaAnnotatedType;
	private final PositiveIntegerInterval cardinality;
	private final boolean isMeta;
	private final RosettaRule ruleReference;
	private final Attribute origin;

	public RAttribute(String name, String definition, List<RosettaDocReference> docReferences, RMetaAnnotatedType rMetaAnnotatedType, PositiveIntegerInterval cardinality, RosettaRule ruleReference, Attribute origin) {
		this(name, definition, docReferences, rMetaAnnotatedType, cardinality, false, ruleReference, origin);
	}
	public RAttribute(String name, String definition, List<RosettaDocReference> docReferences, RMetaAnnotatedType rMetaAnnotatedType, PositiveIntegerInterval cardinality, boolean isMeta, RosettaRule ruleReference, Attribute origin) {
		this.name = name;
		this.definition = definition;
		this.docReferences = docReferences;
		this.rMetaAnnotatedType = rMetaAnnotatedType;
		this.cardinality = cardinality;
		this.isMeta = isMeta;
		this.ruleReference = ruleReference;
		this.origin = origin;
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
		return cardinality.getMax().map(m -> m > 1).orElse(true);
	}
	
	public PositiveIntegerInterval getCardinality() {
		return cardinality;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	public List<RosettaDocReference> getDocReferences() {
		return docReferences;
	}
	

	public RosettaRule getRuleReference() {
		return ruleReference;
	}
	
	public boolean isMeta() {
		return isMeta;
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
				&& Objects.equals(name, other.name)
				&& Objects.equals(rMetaAnnotatedType, other.rMetaAnnotatedType)
				&& Objects.equals(origin, other.origin);
	}

	@Override
	public String toString() {
		return String.format("RAttribute[name=%s, type=%s, cardinality=%s]", name, rMetaAnnotatedType, cardinality);
	}
}
