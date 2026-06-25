package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.CsvUtil;
import jakarta.inject.Inject;
import org.eclipse.xtext.validation.Check;
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.util.IResourceScopeCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaFunctionExtensions functionExtensions;
    @Inject
    private CsvUtil csvUtil;
    @Inject
    private RObjectFactory rObjectFactory;
    @Inject
    private WarningSuppressionHelper warningSuppressionHelper;
    @Inject
    private TransformAnnotationHelper transformAnnotationHelper;
    @Inject
    private IResourceScopeCache cache;

    /**
     * Cache key for the set of function URIs that are referenced (called) anywhere in the resource set.
     * The set is computed once per validated resource and reused across every function in it.
     */
    private static final String REFERENCED_FUNCTIONS_CACHE_KEY =
            "com.regnosys.rosetta.validation.FunctionValidator.referencedFunctions";

    @Check
    public void checkFunctionNameStartsWithCapital(Function func) {
        boolean suppressed = warningSuppressionHelper.isCapitalisationSuppressed(func);
        if (!suppressed && Character.isLowerCase(func.getName().charAt(0))) {
            warning("Function name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
        }
    }
    
    @Check
    public void checkCsvIngestionInput(Function function) {
        if (hasCsvTransformAnnotation(function, "ingest")) {
            if (!function.getInputs().isEmpty()) {
                checkAttributeIsTabular(function.getInputs().get(0), "The input of a CSV ingest function");
            }
        }
    }
    @Check
    public void checkCsvProjectionOutput(Function function) {
        if (hasCsvTransformAnnotation(function, "projection")) {
            Attribute output = function.getOutput();
            if (output != null) {
                checkAttributeIsTabular(output, "The output of a CSV projection function");
            }
        }
    }
    private void checkAttributeIsTabular(Attribute attribute, String attributeDescription) {
        RAttribute attr = rObjectFactory.buildRAttribute(attribute);
        RType t = attr.getRMetaAnnotatedType().getRType();
        if (!(t instanceof RDataType dt)) {
            error(attributeDescription + " must be a complex type", attribute, ROSETTA_TYPED__TYPE_CALL);
        } else {
            List<RAttribute> nonSimple = csvUtil.getNonSimpleAttributes(dt);
            if (!nonSimple.isEmpty()) {
                String attrs = nonSimple.stream().map(a -> "`" + a.getName() + "`").collect(Collectors.joining(", "));
                error(attributeDescription + " must be a tabular type. Type `" + dt.getName() + "` has non-simple attributes: " + attrs, attribute, ROSETTA_TYPED__TYPE_CALL);
            }
        }
        if (attr.isMulti()) {
            error(attributeDescription + " must be single cardinality", attribute, SimplePackage.Literals.ATTRIBUTE__CARD);
        }
    }
    private boolean hasCsvTransformAnnotation(Function function, String transformName) {
        return functionExtensions.getTransformAnnotations(function)
                .stream()
                .filter(a -> a.getAttribute() != null && "CSV".equals(a.getAttribute().getName()))
                .map(AnnotationRef::getAnnotation)
                .anyMatch(a -> a != null && transformName.equals(a.getName()));
    }
    
    @Check
    public void checkUnusedFunction(Function function) {
        // Only check top-level functions (not function extensions)
        if (function.getSuperFunction() != null) {
            return;
        }
        // Allow explicit opt-out via [suppressWarnings unused]
        if (warningSuppressionHelper.isUnusedSuppressed(function)) {
            return;
        }
        // Skip functions with a transform annotation (ingest/projection) — these
        // are entry points called from outside the model.
        if (!function.getTransform().isEmpty()) {
            return;
        }
        // Skip functions with no body and no codeImplementation — they will already be
        // reported by warnWhenEmptyFunctionsDontHaveCodeImplementationAnnotation.
        if (function.getOutput() != null
                && function.getOutput().getName() != null
                && function.getOperations().isEmpty()) {
            return;
        }
        Resource resource = function.eResource();
        if (resource == null || resource.getResourceSet() == null) {
            return;
        }
        if (!getReferencedFunctionUris(resource).contains(EcoreUtil.getURI(function))) {
            warning("Function '" + function.getName() + "' is never used",
                    function, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.UNUSED_FUNCTION);
        }
    }

    /**
     * Collects the URIs of every function that is the target of a symbol reference (i.e. is called)
     * anywhere in the resource set.
     *
     * <p>The previous implementation re-walked every AST node of every resource <em>per function</em>,
     * giving O(functions × nodes) behaviour on every validation. This walks the resource set once,
     * caches the resulting set per validated resource via {@link IResourceScopeCache}, and turns each
     * subsequent per-function check into an O(1) set lookup.
     *
     * <p>Note: the cross-reference index cannot be used here. Rosetta's
     * {@code RosettaResourceDescriptionStrategy} deliberately does not descend into expressions, so
     * function-call ({@code RosettaSymbolReference}) references are absent from the index. The live AST
     * is therefore the only source that sees function usages.
     *
     * <p>The set is cached per validated resource and evicted when that resource changes. Trade-off:
     * adding the first call from <em>another</em> file does not evict this resource's cache, so the
     * marker may briefly lag until this file is next touched — acceptable for a {@code Hint}-level
     * marker, and it self-heals on the next edit.
     */
    private Set<URI> getReferencedFunctionUris(Resource resource) {
        return cache.get(REFERENCED_FUNCTIONS_CACHE_KEY, resource, () -> {
            Set<URI> referenced = new HashSet<>();
            for (Resource r : resource.getResourceSet().getResources()) {
                if (r.getContents().isEmpty()) {
                    continue;
                }
                for (TreeIterator<EObject> it = r.getAllContents(); it.hasNext();) {
                    EObject obj = it.next();
                    if (obj instanceof RosettaSymbolReference ref
                            && !ref.eIsProxy()
                            && ref.getSymbol() instanceof Function calledFunction) {
                        referenced.add(EcoreUtil.getURI(calledFunction));
                    }
                }
            }
            return referenced;
        });
    }

    @Check
    public void warnWhenEmptyFunctionsDontHaveCodeImplementationAnnotation(Function function) {
        if (function.getOutput() != null && function.getOutput().getName() != null) {
            Annotated annotated = (Annotated) function;
            boolean hasCodeImplementationAnnotation = annotated.getAnnotations()
            .stream()
            .map(aRef -> aRef.getAnnotation())
            .anyMatch(a -> "codeImplementation".equals(a.getName()));
            
            if (function.getOperations().isEmpty() && !hasCodeImplementationAnnotation) {
                warning("A function should specify an implementation, or they should be annotated with codeImplementation", function, ROSETTA_NAMED__NAME);
            }
            
            if (!function.getOperations().isEmpty() && hasCodeImplementationAnnotation) {
                warning("Functions annotated with codeImplementation should not have any setter operations as they will be overriden", function, ROSETTA_NAMED__NAME);
            }          
        }
    }
}
