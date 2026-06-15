package com.regnosys.rosetta.config.file;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.config.RuneDependencyConfiguration;
import com.regnosys.rosetta.config.RuneGeneratorsConfiguration;
import com.regnosys.rosetta.config.RuneModelConfiguration;

public abstract class RuneConfigurationMixin {
	@JsonCreator
	public RuneConfigurationMixin(
			@JsonProperty("model") RuneModelConfiguration model,
			@JsonProperty("dependencies") List<RuneDependencyConfiguration> dependencies,
			@JsonProperty("generators") RuneGeneratorsConfiguration generators) {}
}
