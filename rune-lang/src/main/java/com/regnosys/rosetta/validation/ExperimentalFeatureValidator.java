package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.experimental.ExperimentalFeature;
import com.regnosys.rosetta.experimental.ExperimentalFeatureService;
import com.regnosys.rosetta.rosetta.RosettaScope;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;

public class ExperimentalFeatureValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private ExperimentalFeatureService experimentalFeatureService;
    
    @Check
    public void checkScopes(RosettaScope scope) {
        errorIfDisabled(ExperimentalFeature.SCOPES, scope, null);
    }
    
    private void errorIfDisabled(ExperimentalFeature experimentalFeature, EObject object, EStructuralFeature feature) {
        if (!experimentalFeatureService.isEnabled(experimentalFeature)) {
            error(StringUtils.capitalize(experimentalFeature.getFeatureName()) + " are an experimental feature, and are not enabled for this project", object, feature);
        }
    }
}
