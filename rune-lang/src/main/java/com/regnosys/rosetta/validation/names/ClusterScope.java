package com.regnosys.rosetta.validation.names;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;

import java.util.function.Function;

public interface ClusterScope {
    default boolean acceptCluster(IEObjectDescription description, EClass clusterType) {
        return true;
    }
    
    Function<IEObjectDescription, ISelectable> getScope(Resource resource, EClass clusterType);
}
