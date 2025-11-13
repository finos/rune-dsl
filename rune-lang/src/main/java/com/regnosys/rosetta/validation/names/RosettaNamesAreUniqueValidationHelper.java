package com.regnosys.rosetta.validation.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.validation.NamesAreUniqueValidationHelper;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.regnosys.rosetta.resource.RosettaResourceDescriptionStrategy.IN_OVERRIDDEN_NAMESPACE;

@Singleton // Singleton because Xtext's default implementation is a singleton too.
public class RosettaNamesAreUniqueValidationHelper extends NamesAreUniqueValidationHelper {
    /**
     * Object classes that should not be checked for uniqueness.
     */
    // TODO: move into RosettaUniqueNamesConfig
    private static final Set<EClass> IGNORED_TYPES = Set.of(
            RosettaPackage.eINSTANCE.getRosettaModel(),
            RosettaPackage.eINSTANCE.getRosettaMetaType(),
            SimplePackage.eINSTANCE.getFunctionDispatch()
    );
    /**
     * User-friendly labels for each cluster type.
     */
    // TODO: also use these labels for linking diagnostics
    private static final Map<EClass, String> TYPE_LABELS = ImmutableMap.<EClass, String>builder()
            .put(RosettaPackage.eINSTANCE.getRosettaModel(), "namespace")
            .put(RosettaPackage.eINSTANCE.getRosettaRootElement(), "element")
            .put(RosettaPackage.eINSTANCE.getRosettaSynonymSource(), "synonym source")
            .put(RosettaPackage.eINSTANCE.getRosettaExternalRuleSource(), "rule source")
            .put(RosettaPackage.eINSTANCE.getRosettaCallableWithArgs(), "function")
            .put(RosettaPackage.eINSTANCE.getRosettaType(), "type")
            .put(SimplePackage.eINSTANCE.getData(), "type")
            .put(SimplePackage.eINSTANCE.getAnnotation(), "annotation")
            .put(RosettaPackage.eINSTANCE.getRosettaBody(), "body")
            .put(RosettaPackage.eINSTANCE.getRosettaCorpus(), "corpus")
            .put(RosettaPackage.eINSTANCE.getRosettaSegment(), "segment")
            .put(RosettaPackage.eINSTANCE.getRosettaScope(), "scope")
            .put(SimplePackage.eINSTANCE.getAttribute(), "attribute")
            .build();
    
    private final RosettaUniqueNamesConfig config;
    private final Set<EClass> clusterTypes;
    
    @Inject
    public RosettaNamesAreUniqueValidationHelper(RosettaUniqueNamesConfig config) {
        this.config = config;
        this.clusterTypes = config.getDuplicationClusters().keySet();
    }

    @Override
    protected EClass getClusterType(IEObjectDescription description) {
        EClass associatedType = this.getAssociatedClusterType(description);
        if (associatedType == null) {
            return null;
        }
        DuplicationCluster cluster = config.getDuplicationCluster(associatedType);
        if (cluster != null && cluster.clusterScope().acceptCluster(description, associatedType)) {
            return associatedType;
        }
        return null;
    }

    protected EClass getAssociatedClusterType(IEObjectDescription description) {
        if (IGNORED_TYPES.contains(description.getEClass()) || isInOverriddenNamespace(description)) {
            return null;
        }
        return clusterTypes.stream()
                .filter(clusterType -> isPartOfCluster(description, clusterType))
                .findFirst()
                .orElse(null);
    }
    
    protected boolean isPartOfCluster(IEObjectDescription description, EClass clusterType) {
        return clusterType.isSuperTypeOf(description.getEClass()) && config.getDuplicationCluster(clusterType).clusterScope().acceptCluster(description, clusterType);
    }
    
    @Override
    protected void doCheckUniqueIn(IEObjectDescription description, Context context,
                                   ValidationMessageAcceptor acceptor) {
        EObject object = description.getEObjectOrProxy();
        Preconditions.checkArgument(!object.eIsProxy());

        EClass clusterType = getClusterType(description);
        if (clusterType == null) {
            return;
        }
        ISelectable validationScope = context.getValidationScope(description, clusterType);
        if (validationScope.isEmpty()) {
            return;
        }
        boolean caseSensitive = context.isCaseSensitive(object, clusterType);
        Iterable<IEObjectDescription> sameNames = validationScope.getExportedObjects(clusterType, description.getName(),
                !caseSensitive);
        if (sameNames instanceof Collection<?>) {
            if (((Collection<?>) sameNames).size() <= 1) {
                return;
            }
        }
        for (IEObjectDescription candidate : sameNames) {
            EObject otherObject = candidate.getEObjectOrProxy();
            if (object != otherObject && isPartOfCluster(candidate, clusterType)
                && !otherObject.eIsProxy() || !candidate.getEObjectURI().equals(description.getEObjectURI())) {
                if (isDuplicate(description, candidate)) {
                    createDuplicateNameError(description, clusterType, acceptor);
                    return;
                }
            }
        }
    }
    
    @Override
    protected boolean isDuplicate(IEObjectDescription description, IEObjectDescription candidate) {
        return !isInOverriddenNamespace(candidate);
    }
    
    private boolean isInOverriddenNamespace(IEObjectDescription description) {
        return "true".equals(description.getUserData(IN_OVERRIDDEN_NAMESPACE));
    }

    @Override
    protected String getTypeLabel(EClass clusterType) {
        String label = TYPE_LABELS.get(clusterType);
        return label != null ? label : super.getTypeLabel(clusterType);
    }
}
