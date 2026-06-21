package com.regnosys.rosetta.experimental;

import com.regnosys.rosetta.utils.RuneConfigurationHolder;
import jakarta.inject.Inject;

public class ExperimentalFeatureService {
    @Inject
    private RuneConfigurationHolder configuration;

    public boolean isEnabled(ExperimentalFeature feature) {
        return configuration.get().getModel().getEnableExperimentalFeatures().contains(feature);
    }
}
