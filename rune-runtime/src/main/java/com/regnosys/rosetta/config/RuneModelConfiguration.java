package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regnosys.rosetta.experimental.ExperimentalFeature;
import com.rosetta.model.lib.transform.SerializationFormat;

import org.apache.commons.lang3.Validate;

public class RuneModelConfiguration {
	private final String name;
	private final List<ExperimentalFeature> enableExperimentalFeatures;
	private final SerializationFormat defaultSerialisationFormat;

	public RuneModelConfiguration(String name, List<ExperimentalFeature> enableExperimentalFeatures) {
		this(name, enableExperimentalFeatures, null);
	}

	@JsonCreator
	public RuneModelConfiguration(
			@JsonProperty("name") String name,
			@JsonProperty("enableExperimentalFeatures") List<ExperimentalFeature> enableExperimentalFeatures,
			@JsonProperty("defaultSerialisationFormat") SerializationFormat defaultSerialisationFormat) {
		Objects.requireNonNull(name);
		this.name = name;
		this.enableExperimentalFeatures = enableExperimentalFeatures == null ? Collections.emptyList() : enableExperimentalFeatures;
		Validate.noNullElements(this.enableExperimentalFeatures);
		this.defaultSerialisationFormat = defaultSerialisationFormat;
	}

	public String getName() {
		return name;
	}

	public List<ExperimentalFeature> getEnableExperimentalFeatures() {
		return enableExperimentalFeatures;
	}

	public SerializationFormat getDefaultSerialisationFormat() {
		return defaultSerialisationFormat;
	}
}
