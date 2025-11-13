package com.regnosys.rosetta.experimental;

import com.regnosys.rosetta.config.RosettaModelConfiguration;
import jakarta.inject.Inject;

public class ExperimentalFeatureService {
    @Inject
    private RosettaModelConfiguration modelConfiguration;
    
    public boolean isEnabled(ExperimentalFeature feature) {
        return modelConfiguration.getEnableExperimentalFeatures().contains(feature);
    }
}
