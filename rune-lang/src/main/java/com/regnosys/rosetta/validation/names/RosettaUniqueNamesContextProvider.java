package com.regnosys.rosetta.validation.names;

import jakarta.inject.Inject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.DefaultUniqueNameContext;
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper.Context;

@SuppressWarnings("UnstableApiUsage")
public class RosettaUniqueNamesContextProvider extends DefaultUniqueNameContext.Global {
    @Inject
    private RosettaUniqueNamesConfig uniqueNamesConfig;
    
    @Override
    public Context tryGetContext(Resource resource, CancelIndicator cancelIndicator) {
        Context globalContext = super.tryGetContext(resource, cancelIndicator);
        if (globalContext == null) {
            return null;
        }
        return new RosettaUniqueNamesContext(globalContext, uniqueNamesConfig.getDuplicationClusters(), cancelIndicator);
    }
}
