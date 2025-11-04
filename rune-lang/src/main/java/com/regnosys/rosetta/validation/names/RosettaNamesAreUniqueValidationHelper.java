package com.regnosys.rosetta.validation.names;

import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.validation.NamesAreUniqueValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.regnosys.rosetta.resource.RosettaResourceDescriptionStrategy.IN_OVERRIDDEN_NAMESPACE;

@Singleton // Singleton because Xtext's default implementation is a singleton too.
public class RosettaNamesAreUniqueValidationHelper extends NamesAreUniqueValidationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RosettaNamesAreUniqueValidationHelper.class);
    
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
        LOGGER.info("Getting cluster type for {}", description);
        if (IGNORED_TYPES.contains(description.getEClass()) || isInOverriddenNamespace(description)) {
            return null;
        }
        EClass associatedType = this.getAssociatedClusterType(description.getEClass());
        if (associatedType == null) {
            return null;
        }
        DuplicationCluster cluster = config.getDuplicationCluster(associatedType);
        if (cluster != null && cluster.clusterScope().acceptCluster(description, associatedType)) {
            return associatedType;
        }
        return null;
    }

    @Override
    protected EClass getAssociatedClusterType(EClass eClass) {
        if (IGNORED_TYPES.contains(eClass)) {
            return null;
        }
        if (clusterTypes.contains(eClass)) {
            return eClass;
        }
        return clusterTypes.stream()
                .filter(t -> t.isSuperTypeOf(eClass))
                .findFirst()
                .orElse(EcorePackage.eINSTANCE.getEObject());
    }
    
    @Override
    protected boolean isDuplicate(IEObjectDescription description, IEObjectDescription candidate) {
        LOGGER.info("Checking if {} is a duplicate of {}", candidate, description);
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
