package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RuneSchemaConfigurationMixin {
	@JsonCreator
	public RuneSchemaConfigurationMixin(
			@JsonProperty("schema") String schema,
			@JsonProperty("configPath") String configPath) {}
}
