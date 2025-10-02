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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.rosetta.model.lib.ModelSymbolId;

public class RosettaXMLConfiguration {
	@JsonAnyGetter
	private final SortedMap<ModelSymbolId, TypeXMLConfiguration> typeConfigMap;

	public RosettaXMLConfiguration(Map<ModelSymbolId, TypeXMLConfiguration> typeConfigMap) {
		this.typeConfigMap = new TreeMap<>(typeConfigMap);
	}
	
	public static RosettaXMLConfiguration load(InputStream input) throws IOException {
		ObjectMapper xmlConfigurationMapper = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .addModule(new Jdk8Module()) // because RosettaXMLConfiguration contains `Optional` types.
                .serializationInclusion(JsonInclude.Include.NON_ABSENT) // because we want to interpret an absent value as `Optional.empty()`.
                .build();
        return xmlConfigurationMapper.readValue(input, RosettaXMLConfiguration.class);
	}

	public Optional<TypeXMLConfiguration> getConfigurationForType(ModelSymbolId symbolId) {
		return Optional.ofNullable(typeConfigMap.get(symbolId));
	}
	
	public List<ModelSymbolId> getSubstitutionsFor(String substitutionGroup) {
		return typeConfigMap.entrySet().stream()
			.filter(e -> e.getValue().getSubstitutionGroup().map(g -> g.equals(substitutionGroup)).orElse(false))
			.map(e -> e.getKey())
			.collect(Collectors.toList());
	}
	@Deprecated // use getSubstitutionsFor instead.
	public List<ModelSymbolId> getSubstitutionsForType(ModelSymbolId symbolId) {
		return typeConfigMap.entrySet().stream()
			.filter(e -> e.getValue().getSubstitutionFor().map(t -> t.equals(symbolId)).orElse(false))
			.map(e -> e.getKey())
			.collect(Collectors.toList());
	}

	@JsonIgnore
	public SortedMap<ModelSymbolId, TypeXMLConfiguration> getTypeConfigMap() {
		return typeConfigMap;
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
