package com.regnosys.rosetta.validation.uniquenames;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import jakarta.inject.Singleton;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.validation.NamesAreUniqueValidationHelper;

import java.util.Map;
import java.util.Set;

@Singleton // Singleton because Xtext's default implementation is a singleton too.
public class RosettaNamesAreUniqueValidationHelper extends NamesAreUniqueValidationHelper {
    /**
     * Object classes that should not be checked for uniqueness.
     */
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
            .put(RosettaPackage.eINSTANCE.getRosettaScope(), "scope")
            .put(RosettaPackage.eINSTANCE.getRosettaSynonymSource(), "synonym source")
            .put(RosettaPackage.eINSTANCE.getRosettaExternalRuleSource(), "rule source")
            .put(SimplePackage.eINSTANCE.getFunction(), "function") // TODO: cluster callable elements?
            .put(RosettaPackage.eINSTANCE.getRosettaRule(), "rule")
            .put(RosettaPackage.eINSTANCE.getRosettaExternalFunction(), "external function")
            .put(SimplePackage.eINSTANCE.getData(), "type")
            .put(SimplePackage.eINSTANCE.getChoice(), "choice")
            .put(RosettaPackage.eINSTANCE.getRosettaEnumeration(), "enum")
            .put(RosettaPackage.eINSTANCE.getRosettaTypeAlias(), "type alias")
            .put(SimplePackage.eINSTANCE.getAnnotation(), "annotation")
            .put(RosettaPackage.eINSTANCE.getRosettaBasicType(), "basic type")
            .put(RosettaPackage.eINSTANCE.getRosettaRecordType(), "record type")
            .put(RosettaPackage.eINSTANCE.getRosettaBody(), "body")
            .put(RosettaPackage.eINSTANCE.getRosettaCorpus(), "corpus")
            .put(RosettaPackage.eINSTANCE.getRosettaSegment(), "segment")
            .put(SimplePackage.eINSTANCE.getAttribute(), "attribute")
            .build();
    // TODO: add all root elements
    // TODO: check locally for non-root elements
    // TODO: make case insensitive
    // TODO: support namespace overrides
    // TODO: remove custom checks
    
    @Override
    protected ImmutableSet<EClass> getClusterTypes() {
        return ImmutableSet.copyOf(TYPE_LABELS.keySet());
    }

    @Override
    protected EClass getClusterType(IEObjectDescription description) {
        if (IGNORED_TYPES.contains(description.getEClass())) {
            return null;
        }
        return super.getClusterType(description);
    }

    @Override
    protected String getTypeLabel(EClass eClass) {
        String label = TYPE_LABELS.get(eClass);
        return label != null ? label : super.getTypeLabel(eClass);
    }
}
