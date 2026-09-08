package com.regnosys.rosetta.validation.names;

import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.validation.RosettaIssueCodes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configure which unique name checks to run.
 */
@Singleton
public class RosettaUniqueNamesConfig {
    private final ClusterScopes scopes;
    private final Map<EClass, DuplicationCluster> duplicationClusters = new HashMap<>();
    
    @Inject
    public RosettaUniqueNamesConfig(ClusterScopes scopes) {
        this.scopes = scopes;
        initialize();
    }
    
    protected void initialize() {
        // Note about case sensitivity: in general, if a generator uses the name of an element
        // to generate a file, then that name should be unique in a case-insensitive way,
        // otherwise case-insensitive file systems such as Mac and Windows will have problems.
        // For example, the Java generator generates a Java file for each type and enum, so these
        // should be checked for case-insensitive uniqueness. On the other hand, it does not
        // generate a file for an attribute, so we can check those for case-sensitive uniqueness.
        
        // Check rule sources have a unique name
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaExternalRuleSource(), false);
        
        // Check functions, rules and other callable things have a unique name
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaCallableWithArgs(), false);
        
        // Check types, enums, choice types, aliases and other types have a unique name
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaType(), false);
        
        // Check annotations have a unique name
        addGlobalCheck(SimplePackage.eINSTANCE.getAnnotation(), false);
        
        // Check regulatory reference constituents have a unique name
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaBody(), true);
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaCorpus(), true);
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaSegment(), true);
        
        // Check scopes have a unique name
        addGlobalCheck(RosettaPackage.eINSTANCE.getRosettaScope(), false);

        // Check serialisation schemas have a unique name. A schema does not generate a file,
        // so case-sensitive uniqueness is sufficient.
        addGlobalCheck(RosettaPackage.eINSTANCE.getSchema(), true);
        
        // Check attributes in data have a unique name
        addLocalCheck(SimplePackage.eINSTANCE.getAttribute(), Attribute.class, this::getDirectDataContainer, Data::getAttributes, true);
        
        // Check function symbols have a unique name
        addLocalCheck(RosettaPackage.eINSTANCE.getRosettaSymbol(), RosettaSymbol.class, this::getFunctionContainer, this::getFunctionSymbols, true);

        // Check conditions in a type or function have a unique name. This is a warning rather than an error,
        // because some existing production models already have duplicate condition names.
        addLocalCheck(SimplePackage.eINSTANCE.getCondition(), Condition.class, this::getConditionContainer, this::getConditionSiblings, true,
                DuplicationCluster.Severity.WARNING, RosettaIssueCodes.DUPLICATE_CONDITION_NAME);
    }

    private Data getDirectDataContainer(Attribute attr) {
        return containerOfType(attr, Data.class);
    }

    private Function getFunctionContainer(RosettaSymbol symbol) {
        return EcoreUtil2.getContainerOfType(symbol, Function.class);
    }
    private Iterable<RosettaSymbol> getFunctionSymbols(Function function) {
        return EcoreUtil2.getAllContentsOfType(function, RosettaSymbol.class);
    }

    private Object getConditionContainer(Condition condition) {
        EObject container = condition.eContainer();
        if (container instanceof Function function) {
            return new ConditionListKey(function, condition.isPostCondition());
        }
        return container;
    }
    private Iterable<Condition> getConditionSiblings(Object container) {
        if (container instanceof ConditionListKey key) {
            return key.postCondition() ? key.function().getPostConditions() : key.function().getConditions();
        }
        if (container instanceof RosettaTypeWithConditions typeWithConditions) {
            return typeWithConditions.getConditions();
        }
        return List.of();
    }
    private record ConditionListKey(Function function, boolean postCondition) {}

    private <T> T containerOfType(EObject eObject, Class<T> clazz) {
        EObject parent = eObject.eContainer();
        if (clazz.isInstance(parent)) {
            return clazz.cast(parent);
        }
        return null;
    }
    
    public Map<EClass, DuplicationCluster> getDuplicationClusters() {
        return duplicationClusters;
    }
    public DuplicationCluster getDuplicationCluster(EClass clusterType) {
        return duplicationClusters.get(clusterType);
    }
    
    protected void addGlobalCheck(EClass clusterType, boolean caseSensitive) {
        addGlobalCheck(clusterType, caseSensitive, DuplicationCluster.Severity.ERROR, null);
    }
    protected void addGlobalCheck(EClass clusterType, boolean caseSensitive, DuplicationCluster.Severity severity, String issueCode) {
        duplicationClusters.put(clusterType, new DuplicationCluster(clusterType, scopes.global(), caseSensitive, severity, issueCode));
    }
    protected <Parent, Child extends RosettaNamed> void addLocalCheck(EClass clusterType, Class<Child> childClass, java.util.function.Function<Child, Parent> getParent, java.util.function.Function<Parent, Iterable<Child>> getChildren, boolean caseSensitive) {
        addLocalCheck(clusterType, childClass, getParent, getChildren, caseSensitive, DuplicationCluster.Severity.ERROR, null);
    }
    protected <Parent, Child extends RosettaNamed> void addLocalCheck(EClass clusterType, Class<Child> childClass, java.util.function.Function<Child, Parent> getParent, java.util.function.Function<Parent, Iterable<Child>> getChildren, boolean caseSensitive, DuplicationCluster.Severity severity, String issueCode) {
        duplicationClusters.put(clusterType, new DuplicationCluster(clusterType, scopes.local(childClass, getParent, getChildren), caseSensitive, severity, issueCode));
    }
}
