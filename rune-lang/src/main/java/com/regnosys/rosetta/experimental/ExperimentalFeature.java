package com.regnosys.rosetta.experimental;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExperimentalFeature {
    SCOPES("scopes");

    private final String featureName;

    private ExperimentalFeature(String featureName) {
        this.featureName = featureName;
    }

    @JsonValue
    public String getFeatureName() {
        return featureName;
    }
}
