package com.regnosys.rosetta.validation.names;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper.Context;
import org.eclipse.xtext.validation.LocalUniqueNameContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class RosettaUniqueNamesContext implements Context {
    private final Context globalContext;
    private final Map<EClass, DuplicationCluster> duplicationClusters;
    private final CancelIndicator cancelIndicator;
    
    private final Map<LocalContextKey, ISelectable> localValidationScopes = new HashMap<>();
    private record LocalContextKey(EObject container, EClass clusterType) {}
    
    public RosettaUniqueNamesContext(Context globalContext, Map<EClass, DuplicationCluster> duplicationClusters, CancelIndicator cancelIndicator) {
        this.globalContext = globalContext;
        this.duplicationClusters = duplicationClusters;
        this.cancelIndicator = cancelIndicator;
    }
    
    @Override
    public ISelectable getValidationScope(IEObjectDescription description, EClass clusterType) {
        DuplicationCluster cluster = duplicationClusters.get(clusterType);
        if (cluster.scope() == ValidationScopeEnum.GLOBAL) {
            return globalContext.getValidationScope(description, clusterType);
        }
        return getLocalValidationScope(description, clusterType);
    }
    
    private ISelectable getLocalValidationScope(IEObjectDescription description, EClass clusterType) {
        EObject container = description.getEObjectOrProxy().eContainer();
        if (container == null) {
            return null;
        }
        LocalContextKey key = new LocalContextKey(container, clusterType);
        return localValidationScopes.computeIfAbsent(key, c -> 
                new PatchedLocalUniqueNameContext(
                        Iterables.filter(container.eContents(), obj -> clusterType.isSuperTypeOf(obj.eClass())),
                        RosettaUniqueNamesContext::tryGetName,
                        cancelIndicator).getValidationScope(description, clusterType));
    }

    @Override
    public Iterable<IEObjectDescription> getObjectsToValidate() {
        return globalContext.getObjectsToValidate();
    }

    @Override
    public CancelIndicator cancelIndicator() {
        return cancelIndicator;
    }

    @Override
    public boolean isCaseSensitive(EObject object, EClass clusterType) {
        return duplicationClusters.get(clusterType).caseSensitive();
    }

    private static String tryGetName(EObject obj) {
        if (obj instanceof RosettaNamed named) {
            return named.getName();
        }
        return null;
    }
    
    // TODO: contribute to Xtext
    private static class PatchedLocalUniqueNameContext extends LocalUniqueNameContext {
        public <T extends EObject> PatchedLocalUniqueNameContext(Iterable<T> objects, Function<T, String> nameFunction, CancelIndicator ci) {
            super(objects, nameFunction, ci);
        }

        @Override
        public Iterable<IEObjectDescription> getExportedObjects(EClass type, QualifiedName name, boolean ignoreCase) {
            return super.getExportedObjects(type, QualifiedName.create(name.getLastSegment()), ignoreCase);
        }
    }
}
