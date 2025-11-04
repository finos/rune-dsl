package com.regnosys.rosetta.validation.names;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper.Context;

import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class RosettaUniqueNamesContext implements Context {
    private final IResourceDescription resourceDescription;
    private final Map<EClass, Function<IEObjectDescription, ISelectable>> validationScopes;
    private final Map<EClass, DuplicationCluster> duplicationClusters;
    private final CancelIndicator cancelIndicator;
    
    public RosettaUniqueNamesContext(IResourceDescription resourceDescription, Map<EClass, Function<IEObjectDescription, ISelectable>> validationScopes, Map<EClass, DuplicationCluster> duplicationClusters, CancelIndicator cancelIndicator) {
        this.resourceDescription = resourceDescription;
        this.validationScopes = validationScopes;
        this.duplicationClusters = duplicationClusters;
        this.cancelIndicator = cancelIndicator;
    }
    
    @Override
    public ISelectable getValidationScope(IEObjectDescription description, EClass clusterType) {
        return validationScopes.get(clusterType).apply(description);
    }

    @Override
    public Iterable<IEObjectDescription> getObjectsToValidate() {
        return resourceDescription.getExportedObjects(); // TODO: also include local elements
    }

    @Override
    public CancelIndicator cancelIndicator() {
        return cancelIndicator;
    }

    @Override
    public boolean isCaseSensitive(EObject object, EClass clusterType) {
        return duplicationClusters.get(clusterType).caseSensitive();
    }
}
