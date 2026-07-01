package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.config.RuneSchemaConfiguration;

public abstract class RuneNamespaceConfigurationMixin {
	@JsonCreator
	public RuneNamespaceConfigurationMixin(
			@JsonProperty("id") String id,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("readOnly") boolean readOnly,
			@JsonProperty("schemaConfig") RuneSchemaConfiguration schemaConfig) {}
}
