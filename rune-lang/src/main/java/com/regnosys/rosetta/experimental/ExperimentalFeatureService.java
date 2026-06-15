package com.regnosys.rosetta.experimental;

import com.regnosys.rosetta.config.RuneModelConfiguration;
import jakarta.inject.Inject;

public class ExperimentalFeatureService {
    @Inject
    private RuneModelConfiguration modelConfiguration;
    
    public boolean isEnabled(ExperimentalFeature feature) {
        return modelConfiguration.getEnableExperimentalFeatures().contains(feature);
    }
}
