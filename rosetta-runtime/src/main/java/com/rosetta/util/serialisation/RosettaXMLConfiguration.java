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

package com.rosetta.util.serialisation;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.rosetta.model.lib.ModelSymbolId;

public class RosettaXMLConfiguration {
	@JsonAnyGetter
	private final SortedMap<ModelSymbolId, TypeXMLConfiguration> typeConfigMap;

	public RosettaXMLConfiguration(Map<ModelSymbolId, TypeXMLConfiguration> typeConfigMap) {
		this.typeConfigMap = new TreeMap<>(typeConfigMap);
	}

	public Optional<TypeXMLConfiguration> getConfigurationForType(ModelSymbolId symbolId) {
		return Optional.ofNullable(typeConfigMap.get(symbolId));
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeConfigMap);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RosettaXMLConfiguration other = (RosettaXMLConfiguration) obj;
		return Objects.equals(typeConfigMap, other.typeConfigMap);
	}
	
	// Jackson support
	@JsonCreator
	private RosettaXMLConfiguration() {
		this(Collections.emptyMap());
	}
	@JsonAnySetter
	private void add(String qualifiedName, TypeXMLConfiguration config) {
		typeConfigMap.put(ModelSymbolId.fromQualifiedName(qualifiedName), config);
	}
}
