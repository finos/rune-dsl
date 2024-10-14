package com.regnosys.rosetta.tools.modelimport;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportTargetConfig {
	private final String namespace;
	private final String namespaceDefinition;
	private final Map<String, String> nameOverrides;
	
	@JsonCreator
	public ImportTargetConfig(
			@JsonProperty("namespace") String namespace,
			@JsonProperty("namespaceDefinition") String namespaceDefinition,
			@JsonProperty("nameOverrides") Map<String, String> nameOverrides) {
		this.namespace = namespace;
		this.namespaceDefinition = namespaceDefinition;
		this.nameOverrides = nameOverrides;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getNamespaceDefinition() {
		return namespaceDefinition;
	}

	public Map<String, String> getNameOverrides() {
		return nameOverrides;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nameOverrides, namespace, namespaceDefinition);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportTargetConfig other = (ImportTargetConfig) obj;
		return Objects.equals(nameOverrides, other.nameOverrides) && Objects.equals(namespace, other.namespace)
				&& Objects.equals(namespaceDefinition, other.namespaceDefinition);
	}
}
