package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Function;
import jakarta.inject.Inject;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.util.IResourceScopeCache;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Detects functions that are never called anywhere in the resource set.
 *
 * <p>This is intentionally <em>not</em> a validator {@code @Check}: it is consumed only by the
 * editor/LSP layer (see {@code UnusedFunctionResourceValidator} in {@code rune-ide}) so that the
 * result surfaces as a faded marker in the editor without polluting the validation issue stream that
 * batch builds and {@code ValidationTestHelper} tests assert against.
 */
public class UnusedFunctionHelper {
    /**
     * Cache key for the set of function URIs that are referenced (called) anywhere in the resource set.
     * The set is computed once per resource and reused across every function in it.
     */
    private static final String REFERENCED_FUNCTIONS_CACHE_KEY =
            "com.regnosys.rosetta.validation.UnusedFunctionHelper.referencedFunctions";

    private static final String SUPPRESS_UNUSED_ANNOTATION = "suppressUnused";

    @Inject
    private IResourceScopeCache cache;

    /**
     * Returns {@code true} if the given function has no callers anywhere in the resource set and is
     * not exempt (function extension, transform entry point, empty/externally-implemented body, or
     * explicitly opted out via {@code [suppressUnused]}).
     */
    public boolean isUnused(Function function) {
        // Function extensions are not independent entry points.
        if (function.getSuperFunction() != null) {
            return false;
        }
        // Explicit opt-out for intentional entry points (e.g. consumed by downstream models).
        if (isSuppressed(function)) {
            return false;
        }
        // Transform functions (ingest/projection) are entry points called from outside the model.
        if (!function.getTransform().isEmpty()) {
            return false;
        }
        // Functions with no body are reported separately (codeImplementation check); ignore here.
        if (function.getOutput() != null
                && function.getOutput().getName() != null
                && function.getOperations().isEmpty()) {
            return false;
        }
        Resource resource = function.eResource();
        if (resource == null || resource.getResourceSet() == null) {
            return false;
        }
        return !getReferencedFunctionUris(resource).contains(EcoreUtil.getURI(function));
    }

    private boolean isSuppressed(Function function) {
        return function.getAnnotations().stream()
                .map(AnnotationRef::getAnnotation)
                .filter(Objects::nonNull)
                .anyMatch(annotation -> SUPPRESS_UNUSED_ANNOTATION.equals(annotation.getName()));
    }

    /**
     * Collects the URIs of every function that is the target of a symbol reference (i.e. is called)
     * anywhere in the resource set, walking the live AST once and caching the result per resource.
     *
     * <p>The cross-reference index cannot be used: Rosetta's {@code RosettaResourceDescriptionStrategy}
     * does not descend into expressions, so function-call references are absent from it. The live AST
     * is the only source that sees function usages.
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
}
