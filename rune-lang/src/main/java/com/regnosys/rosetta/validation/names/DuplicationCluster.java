package com.regnosys.rosetta.validation.names;

import org.eclipse.emf.ecore.EClass;

public record DuplicationCluster(EClass clusterType, ClusterScope clusterScope, boolean caseSensitive) {
}
