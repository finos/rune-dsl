package com.regnosys.rosetta.experimental;

public enum ExperimentalFeature {
    SCOPES("scopes");
    
    private final String featureName;
    
    private ExperimentalFeature(String featureName) {
        this.featureName = featureName;
    }
    
    public String getFeatureName() {
        return featureName;
    }
}
