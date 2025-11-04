package com.regnosys.rosetta.validation.names;

import com.regnosys.rosetta.rosetta.RosettaNamed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.LocalUniqueNameContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@Singleton
public class ClusterScopes {
    @Inject
    private GlobalClusterScope GLOBAL;
    
    public ClusterScope global() {
        return GLOBAL;
    }
    public <Parent, Child extends RosettaNamed> ClusterScope local(Class<Child> childClass, Function<Child, Parent> getParent, Function<Parent, Iterable<Child>> getChildren) {
        return new LocalClusterScope<Parent, Child>(childClass) {
            @Override
            protected Parent getParent(Child child) {
                return getParent.apply(child);
            }

            @Override
            protected Iterable<Child> getChildren(Parent parent) {
                return getChildren.apply(parent);
            }
        };
    }
    
    @Singleton
    private static class GlobalClusterScope implements ClusterScope {
        @Inject
        private IResourceDescriptionsProvider indexAccess;

        @Override
        public Function<IEObjectDescription, ISelectable> getScope(Resource resource, EClass clusterType) {
            IResourceDescriptions index = getIndex(resource);
            return description -> index;
        }

        protected IResourceDescriptions getIndex(Resource resource) {
            ResourceSet resourceSet = resource.getResourceSet();
            if (resourceSet == null) {
                return null;
            }
            return indexAccess.getResourceDescriptions(resourceSet);
        }
    }
    private static abstract class LocalClusterScope<Parent, Child extends RosettaNamed> implements ClusterScope {
        private final Class<Child> childClass;

        private final Map<LocalScopeKey<Parent>, ISelectable> localValidationScopes = new HashMap<>();
        private record LocalScopeKey<Parent>(Parent container, EClass clusterType) {}

        public LocalClusterScope(Class<Child> childClass) {
            this.childClass = childClass;
        }

        protected abstract Parent getParent(Child child);
        protected abstract Iterable<Child> getChildren(Parent parent);

        @Override
        public boolean acceptCluster(IEObjectDescription description, EClass clusterType) {
            return getParentFromChildDescription(description) != null;
        }

        @Override
        public Function<IEObjectDescription, ISelectable> getScope(Resource resource, EClass clusterType) {
            return description -> {
                Parent parent = getParentFromChildDescription(description);
                LocalScopeKey<Parent> key = new LocalScopeKey<>(parent, clusterType);
                return localValidationScopes.computeIfAbsent(key, k ->
                        new PatchedLocalUniqueNameContext(
                                getChildren(parent),
                                RosettaNamed::getName,
                                CancelIndicator.NullImpl).getValidationScope(description, clusterType));
            };
        }
        
        private Parent getParentFromChildDescription(IEObjectDescription description) {
            EObject object = description.getEObjectOrProxy();
            if (!childClass.isInstance(object)) {
                return null;
            }
            Child child = childClass.cast(object);
            return getParent(child);
        }

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
}
