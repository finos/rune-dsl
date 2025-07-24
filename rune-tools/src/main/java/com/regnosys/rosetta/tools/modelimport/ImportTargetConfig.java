package com.regnosys.rosetta.tools.modelimport;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportTargetConfig {
	private final String namespace;
	private final String namespaceDefinition;
	private final Map<String, String> nameOverrides;
	private final ImportTargetPreferences preferences;
	
	@JsonCreator
	public ImportTargetConfig(
			@JsonProperty("namespace") String namespace,
			@JsonProperty("namespaceDefinition") String namespaceDefinition,
			@JsonProperty("nameOverrides") Map<String, String> nameOverrides,
			@JsonProperty("preferences") ImportTargetPreferences preferences) {
		this.namespace = namespace;
		this.namespaceDefinition = namespaceDefinition;
		this.nameOverrides = nameOverrides == null ? Collections.emptyMap() : nameOverrides;
		this.preferences = preferences == null ? new ImportTargetPreferences(null, null, null, null) : preferences;
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
	
	public ImportTargetPreferences getPreferences() {
		return preferences;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nameOverrides, namespace, namespaceDefinition, preferences);
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
				&& Objects.equals(namespaceDefinition, other.namespaceDefinition)
				&& Objects.equals(preferences, other.preferences);
	}
}
