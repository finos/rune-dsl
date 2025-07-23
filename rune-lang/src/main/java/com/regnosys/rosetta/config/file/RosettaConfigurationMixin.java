package com.regnosys.rosetta.config.file;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.config.RosettaDependencyConfiguration;
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration;
import com.regnosys.rosetta.config.RosettaModelConfiguration;

public abstract class RosettaConfigurationMixin {
	@JsonCreator
	public RosettaConfigurationMixin(
			@JsonProperty("model") RosettaModelConfiguration model,
			@JsonProperty("dependencies") List<RosettaDependencyConfiguration> dependencies,
			@JsonProperty("generators") RosettaGeneratorsConfiguration generators) {}
}
