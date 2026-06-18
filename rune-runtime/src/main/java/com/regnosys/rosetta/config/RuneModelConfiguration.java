package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.experimental.ExperimentalFeature;

import org.apache.commons.lang3.Validate;

public class RuneModelConfiguration {
	private final String name;
    private final List<ExperimentalFeature> enableExperimentalFeatures;

	public RuneModelConfiguration(String name, List<ExperimentalFeature> enableExperimentalFeatures) {
		Objects.requireNonNull(name);
        Validate.noNullElements(enableExperimentalFeatures);
		
		this.name = name;
        this.enableExperimentalFeatures = enableExperimentalFeatures;
	}

	public String getName() {
		return name;
	}
    public List<ExperimentalFeature> getEnableExperimentalFeatures() {
        return enableExperimentalFeatures;
    }
}
