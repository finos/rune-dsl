package com.regnosys.rosetta.validation.names;

import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.DefaultUniqueNameContext;
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper.Context;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class RosettaUniqueNamesContextProvider extends DefaultUniqueNameContext.BaseGlobalContextProvider {
    @Inject
    private RosettaUniqueNamesConfig uniqueNamesConfig;
    
    @Override
    public Context tryGetContext(Resource resource, CancelIndicator cancelIndicator) {
        IResourceDescriptions index = getIndex(resource);
        if (index == null) {
            return null;
        }
        IResourceDescription description = getResourceDescription(resource);
        if (description != null) {
            Map<EClass, DuplicationCluster> duplicationClusters = uniqueNamesConfig.getDuplicationClusters();
            Map<EClass, Function<IEObjectDescription, ISelectable>> validationScopes = duplicationClusters
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().clusterScope().getScope(resource, e.getKey())));
            return new RosettaUniqueNamesContext(description, validationScopes, duplicationClusters, cancelIndicator);
        }
        return null;
    }
}
