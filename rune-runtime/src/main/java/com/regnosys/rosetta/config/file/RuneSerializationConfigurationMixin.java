package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RuneSerializationConfigurationMixin {
	@JsonCreator
	public RuneSerializationConfigurationMixin(
			@JsonProperty("id") String id,
			@JsonProperty("configPath") String configPath) {}
}
