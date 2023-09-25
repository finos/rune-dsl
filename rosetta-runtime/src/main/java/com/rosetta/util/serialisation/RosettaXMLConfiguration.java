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
	
	@JsonCreator
	private RosettaXMLConfiguration() {
		this(Collections.emptyMap());
	}
	public RosettaXMLConfiguration(Map<ModelSymbolId, TypeXMLConfiguration> typeConfigMap) {
		this.typeConfigMap = new TreeMap<>(typeConfigMap);
	}

	public Optional<TypeXMLConfiguration> getConfigurationForType(ModelSymbolId symbolId) {
		return Optional.ofNullable(typeConfigMap.get(symbolId));
	}
	
	@JsonAnySetter
	private void add(String symbolId, TypeXMLConfiguration config) {
		typeConfigMap.put(ModelSymbolId.splitOnDots(symbolId), config);
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
}
