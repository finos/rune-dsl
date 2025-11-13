package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.experimental.ExperimentalFeature;
import jakarta.inject.Inject;

import com.google.inject.ProvidedBy;
import org.apache.commons.lang3.Validate;

@ProvidedBy(RosettaModelConfiguration.Provider.class)
public class RosettaModelConfiguration {
	private final String name;
    private final List<ExperimentalFeature> enableExperimentalFeatures;

	public RosettaModelConfiguration(String name, List<ExperimentalFeature> enableExperimentalFeatures) {
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
	
	public static class Provider implements jakarta.inject.Provider<RosettaModelConfiguration>, javax.inject.Provider<RosettaModelConfiguration> {
		private final RosettaConfiguration config;
		@Inject
		public Provider(RosettaConfiguration config) {
			this.config = config;
		}
		
		@Override
		public RosettaModelConfiguration get() {
			return config.getModel();
		}
		
	}
}
