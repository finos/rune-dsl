package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.experimental.ExperimentalFeature;

import org.apache.commons.lang3.Validate;

public class RuneModelConfiguration {
	private final String name;
	private final List<ExperimentalFeature> enableExperimentalFeatures;

	@JsonCreator
	public RuneModelConfiguration(
			@JsonProperty("name") String name,
			@JsonProperty("enableExperimentalFeatures") List<ExperimentalFeature> enableExperimentalFeatures) {
		Objects.requireNonNull(name);
		this.name = name;
		this.enableExperimentalFeatures = enableExperimentalFeatures == null ? Collections.emptyList() : enableExperimentalFeatures;
		Validate.noNullElements(this.enableExperimentalFeatures);
	}

	public String getName() {
		return name;
	}

	public List<ExperimentalFeature> getEnableExperimentalFeatures() {
		return enableExperimentalFeatures;
	}
}
