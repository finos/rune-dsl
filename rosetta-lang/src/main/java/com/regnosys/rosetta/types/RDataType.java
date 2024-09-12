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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;

public class RDataType extends RAnnotateType implements RObject {
	private final Data data;

	private RType superType = null;
	private ModelSymbolId symbolId = null;
	private List<RAttribute> ownAttributes = null;

	private final ModelIdProvider modelIdProvider;
	private final RObjectFactory objectFactory;
	private final TypeSystem typeSystem;

	// TODO: remove this hack
	private List<RAttribute> additionalAttributes = null;


	public RDataType(final Data data, final ModelIdProvider modelIdProvider, final RObjectFactory objectFactory, final TypeSystem typeSystem) {
		super();
		this.data = data;

		this.modelIdProvider = modelIdProvider;
		this.objectFactory = objectFactory;
		this.typeSystem = typeSystem;
	}
	// TODO: remove this hack
	public RDataType(final Data data, final ModelIdProvider modelIdProvider, final RObjectFactory objectFactory,  final TypeSystem typeSystem, final List<RAttribute> additionalAttributes) {
		this(data, modelIdProvider, objectFactory, typeSystem);
		this.additionalAttributes = additionalAttributes;
	}

	@Override
	public Data getEObject() {
		return this.data;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		if (this.symbolId == null) {
			this.symbolId = modelIdProvider.getSymbolId(data);;
		}
		return this.symbolId;
	}

	public RType getSuperType() {
		if (data.hasSuperType()) {
			if (this.superType == null) {
				this.superType = typeSystem.typeCallToRType(data.getSuperType());
			}
			return this.superType;
		}
		return null;
	}
	/**
	 * Get a list of all super types of this data type, including itself.
	 *
	 * The list is ordered from the most top-level data type to the least (i.e., itself).
	 */
	public List<RType> getAllSuperTypes() {
		LinkedHashSet<RType> reversedResult = new LinkedHashSet<>();
		doGetAllSuperTypes(this, reversedResult);
		List<RType> result = reversedResult.stream().collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(result);
		return result;
	}
	private void doGetAllSuperTypes(RType current, LinkedHashSet<RType> superTypes) {
		if (superTypes.add(current)) {
			RType s = null;
			if (current instanceof RDataType) {
				s = ((RDataType) current).getSuperType();
			} else if (current instanceof RAliasType) {
				s = ((RAliasType) current).getRefersTo();
			}
			if (s != null) {
				doGetAllSuperTypes(s, superTypes);
			}
		}
	}

	/**
	 * Get a list of the attributes defined in this data type. This does not include attributes of any super types.
	 */
	public List<RAttribute> getOwnAttributes() {
		if (ownAttributes == null) {
			if (additionalAttributes != null) {
				this.ownAttributes = Streams.concat(additionalAttributes.stream(), data.getAttributes().stream().map(attr -> objectFactory.buildRAttribute(attr))).collect(Collectors.toList());
			} else {
				this.ownAttributes = data.getAttributes().stream().map(attr -> objectFactory.buildRAttribute(attr)).collect(Collectors.toList());
			}
		}
		return ownAttributes;
	}

	/**
	 * Get a list of all attributes of this data type, including all attributes of its super types.
	 *
	 * The list starts with the attributes of the top-most super type, and ends with the attributes of itself.
	 */
	public List<RAttribute> getAllAttributes() {
		return getAllSuperTypes().stream()
				.filter(s -> s instanceof RDataType)
				.map(s -> (RDataType) s)
				.flatMap(s -> s.getOwnAttributes().stream()).collect(Collectors.toList());
	}

	/**
	 * Get an ordered collection of all attributes of this data type, including all attributes of its super types.
	 * If multiple attributes have the same name, only the last one is kept.
	 *
	 * The collection starts with the attributes of the top-most super type, and ends with the attributes of itself.
	 */
	public Collection<RAttribute> getAllNonOverridenAttributes() {
		Map<String, RAttribute> result = new LinkedHashMap<>();
		getAllAttributes().stream().forEach(a -> result.put(a.getName(), a));
		return result.values();
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.data == null) ? 0 : this.data.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RDataType other = (RDataType) object;
		return Objects.equals(this.data, other.data);
	}
}
