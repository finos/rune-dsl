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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;

public class RDataType extends RType implements RObject {
	private final Data data;
	
	private RDataType superType = null;
	private ModelSymbolId symbolId = null;
	private Map<String, RAttribute> ownAttributes = null;
	private Map<String, RAttribute> allAttributes = null;
	private List<RMetaAttribute> metaAttributes = null;
	
	private final ModelIdProvider modelIdProvider;
	private final RObjectFactory objectFactory;
	private final RosettaTypeProvider typeProvider;

	public RDataType(final Data data, final ModelIdProvider modelIdProvider, final RObjectFactory objectFactory, final RosettaTypeProvider typeProvider) {
		super();
		this.data = data;
		
		this.modelIdProvider = modelIdProvider;
		this.objectFactory = objectFactory;
		this.typeProvider = typeProvider;
	}
	
	@Override
	public Data getEObject() {
		return data;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		if (symbolId == null) {
			symbolId = modelIdProvider.getSymbolId(data);;
		}
		return symbolId;
	}
	
	public List<RMetaAttribute> getMetaAttributes() {
		if (metaAttributes == null) {
			metaAttributes = typeProvider.getRMetaAttributes(data.getAnnotations());
		}
		return metaAttributes;
	}
	public boolean hasMetaAttribute(String name) {
		return getMetaAttributes().stream().anyMatch(m -> m.getName().equals(name));
	}
	
	public RDataType getSuperType() {
		if (data.hasSuperType()) {
			if (superType == null) {
				superType = new RDataType(data.getSuperType(), modelIdProvider, objectFactory, typeProvider);
			}
			return superType;
		}
		return null;
	}
	/**
	 * Get a list of all super types of this data type, including itself.
	 * 
	 * The list is ordered from the most top-level data type to the lowest level (i.e., itself).
	 */
	public List<RDataType> getAllSuperTypes() {
		Set<RDataType> reversedResult = new LinkedHashSet<>();
		doGetAllSuperTypes(this, reversedResult);
		List<RDataType> result = reversedResult.stream().collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(result);
		return result;
	}
	private void doGetAllSuperTypes(RDataType current, Set<RDataType> superTypes) {
		if (superTypes.add(current)) {
			RDataType s = current.getSuperType();
			if (s != null) {
				doGetAllSuperTypes(s, superTypes);
			}
		}
	}
	
	/**
	 * Get an ordered collection of the attributes defined in this data type. This does not include attributes of any super types,
	 * except if the attribute is overridden by this data type.
	 */
	public Collection<RAttribute> getOwnAttributes() {
		return getOwnAttributesAsMap().values();
	}
	
	/**
	 * Find an attribute defined in this data type by name. This does not include attributes of any super types,
	 * except if the attribute is overridden by this data type.
	 * 
	 * If no attribute with the given name exists, the result is null.
	 */
	public RAttribute getOwnAttributeByName(String name) {
		return getOwnAttributesAsMap().get(name);
	}
	
	private Map<String, RAttribute> getOwnAttributesAsMap() {
		if (ownAttributes == null) {
			ownAttributes = data.getAttributes().stream()
					.map(s -> objectFactory.buildRAttributeWithEnclosingType(this, s))
					.collect(Collectors.toMap(
								a -> a.getName(),
								a -> a,
								(a1, a2) -> a1,
								LinkedHashMap::new
							));
		}
		return ownAttributes;
	}
	
	/**
	 * Get a collection of all attributes of this data type, including all attributes of its super types.
	 * 
	 * The collection is ordered and starts with the attributes of the top-most super type, and ends with the attributes of itself.
	 * Attribute overrides replace their respective parent attributes, respecting the original order.
	 */
	public Collection<RAttribute> getAllAttributes() {
		return getAllAttributesAsMap().values();
	}
	
	/**
	 * Find an attribute defined in this data type or in any of its super types by name.
	 * 
	 * If no attribute with the given name exists, the result is null.
	 */
	public RAttribute getAttributeByName(String name) {
		return getAllAttributesAsMap().get(name);
	}
	private Map<String, RAttribute> getAllAttributesAsMap() {
		if (allAttributes == null) {
			allAttributes = buildAllAttributesMap(new HashSet<>());
		}
		return allAttributes;
	}
	private Map<String, RAttribute> buildAllAttributesMap(Set<RDataType> visited) {
		if (!visited.add(this)) {
			return Collections.emptyMap();
		}
		Map<String, RAttribute> result = new LinkedHashMap<>();
		RDataType superType = getSuperType();
		if (superType != null) {
			if (superType.allAttributes == null) {
				superType.allAttributes = superType.buildAllAttributesMap(visited);
			}
			result.putAll(superType.getAllAttributesAsMap());
		}
		result.putAll(getOwnAttributesAsMap());
		return Collections.unmodifiableMap(result);
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
