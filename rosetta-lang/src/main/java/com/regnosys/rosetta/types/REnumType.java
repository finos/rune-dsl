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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;

public class REnumType extends RType implements RObject {
	private final RosettaEnumeration enumeration;
	
	private ModelSymbolId symbolId = null;
	private REnumType parent = null;
	
	private final ModelIdProvider modelIdProvider;
	private final RObjectFactory objectFactory;

	public REnumType(final RosettaEnumeration enumeration, final ModelIdProvider modelIdProvider, final RObjectFactory objectFactory) {
		super();
		this.enumeration = enumeration;
		
		this.modelIdProvider = modelIdProvider;
		this.objectFactory = objectFactory;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		if (this.symbolId == null) {
			this.symbolId = modelIdProvider.getSymbolId(enumeration);
		}
		return this.symbolId;
	}

	@Override
	public RosettaEnumeration getEObject() {
		return this.enumeration;
	}
	
	public REnumType getParent() {
		if (enumeration.getParent() != null) {
			if (parent == null) {
				parent = objectFactory.buildREnumType(enumeration.getParent());
			}
		}
		return parent;
	}
	/**
	 * Get a list of all parents of this enum type, including itself.
	 * 
	 * The list is ordered from the most top-level enumeration to the least (i.e., itself).
	 */
	public List<REnumType> getAllParents() {
		LinkedHashSet<REnumType> reversedResult = new LinkedHashSet<>();
		doGetAllParents(this, reversedResult);
		List<REnumType> result = reversedResult.stream().collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(result);
		return result;
	}
	private void doGetAllParents(REnumType current, LinkedHashSet<REnumType> parents) {
		if (parents.add(current)) {
			REnumType p = current.getParent();
			if (p != null) {
				doGetAllParents(p, parents);
			}
		}
	}

	/**
	 * Get a list of the enum values defined in this enumeration. This does not include enum values of any parents.
	 */
	public List<RosettaEnumValue> getOwnEnumValues() {
		return enumeration.getEnumValues();
	}

	/**
	 * Get a list of all enum values of this enumeration, including all enum values of its parents.
	 * 
	 * The list starts with the enum values of the top-most enumeration, and ends with the enum values of itself.
	 */
	public List<RosettaEnumValue> getAllEnumValues() {
		return getAllParents()
				.stream()
				.flatMap(p -> p.getOwnEnumValues().stream())
				.collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.enumeration == null) ? 0 : this.enumeration.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
        REnumType other = (REnumType)object;
        return Objects.equals(enumeration, other.enumeration);
	}
}
