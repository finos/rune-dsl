package com.regnosys.rosetta.validation.names;

import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;

import java.util.HashMap;
import java.util.Map;

import static com.regnosys.rosetta.validation.names.ValidationScopeEnum.*;

@Singleton
public class RosettaUniqueNamesConfig {
    private final Map<EClass, DuplicationCluster> duplicationClusters = new HashMap<>();
    
    @Inject
    public RosettaUniqueNamesConfig() {
        initialize();
    }
    
    private void initialize() {
        add(RosettaPackage.eINSTANCE.getRosettaSynonymSource(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaExternalRuleSource(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaCallableWithArgs(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaType(), GLOBAL, false);
        add(SimplePackage.eINSTANCE.getAnnotation(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaBody(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaCorpus(), GLOBAL, false);
        add(RosettaPackage.eINSTANCE.getRosettaSegment(), GLOBAL, false);
        
        add(SimplePackage.eINSTANCE.getAttribute(), LOCAL, true);
    }
    
    public Map<EClass, DuplicationCluster> getDuplicationClusters() {
        return duplicationClusters;
    }
    public DuplicationCluster getDuplicationCluster(EClass clusterType) {
        return duplicationClusters.get(clusterType);
    }
    
    private void add(EClass clusterType, ValidationScopeEnum validationScope, boolean caseSensitive) {
        duplicationClusters.put(clusterType, new DuplicationCluster(clusterType, validationScope, caseSensitive));
    }
}
