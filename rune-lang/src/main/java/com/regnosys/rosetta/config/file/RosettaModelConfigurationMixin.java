package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RosettaModelConfigurationMixin {
	@JsonCreator
	public RosettaModelConfigurationMixin(
			@JsonProperty("name") String name) {}
}
