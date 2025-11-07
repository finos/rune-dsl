package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.experimental.ExperimentalFeature;

import java.util.List;

public abstract class RosettaModelConfigurationMixin {
	@JsonCreator
	public RosettaModelConfigurationMixin(
			@JsonProperty("name") String name,
            @JsonProperty("enableExperimentalFeatures") List<ExperimentalFeature> enableExperimentalFeatures) {}
}
