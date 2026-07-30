package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import jakarta.inject.Inject;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.util.IResourceScopeCache;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Detects declarations that are never referenced anywhere in the resource set.
 *
 * <p>Every <em>named</em> root element is a candidate — see {@link #isCandidate}. That is deliberately a rule
 * rather than a list of kinds, so a root element added to the grammar in future is covered by default instead
 * of being a silent omission.
 *
 * <p>This is intentionally <em>not</em> a validator {@code @Check}: it is consumed only by the
 * editor/LSP layer (see {@code UnusedElementResourceValidator} in {@code rune-ide}) so that the
 * result surfaces as a faded marker in the editor without polluting the validation issue stream that
 * batch builds and {@code ValidationTestHelper} tests assert against.
 *
 * <p>Detection is purely syntactic and non-transitive: a declaration referenced only by another
 * unused declaration still counts as used, so peeling dead code takes one edit per layer.
 */
public class UnusedElementHelper {
    /**
     * Cache key for the set of declarations that a single resource references. Keyed per resource so
     * that editing one file only invalidates that file's contribution; answering "is this declaration
     * used" is then a hash lookup against each resource's set rather than a fresh walk of the whole
     * resource set per candidate.
     */
    private static final String OUTGOING_REFERENCES_CACHE_KEY =
            "com.regnosys.rosetta.validation.UnusedElementHelper.outgoingReferences";

    private static final String SUPPRESS_UNUSED_ANNOTATION = "suppressUnused";
    private static final String ROOT_TYPE_ANNOTATION = "rootType";

    @Inject
    private IResourceScopeCache cache;
    @Inject
    private IQualifiedNameProvider qualifiedNameProvider;

    /**
     * Identifies a referenced declaration. Deliberately <em>not</em> an {@link org.eclipse.emf.common.util.URI}:
     * no {@code IFragmentProvider} is bound, so element URIs are EMF's positional fragments
     * ({@code //@elements.3}) and inserting a declaration at the top of a file silently renumbers every
     * declaration below it. Since the sets below are cached per referencing resource and a purely
     * positional change produces no exported-name delta, the files referencing the renumbered ones are
     * not rebuilt — their cached URIs would then point at the wrong declarations and mark used ones as
     * unused. Qualified names survive that; they only change when a declaration is renamed, which does
     * produce a name delta and does rebuild the referencing files.
     *
     * <p>The {@code eClassName} discriminator keeps declarations of different kinds that happen to share
     * a qualified name from being conflated.
     */
    private record ElementId(QualifiedName qualifiedName, String eClassName) {
    }

    /**
     * Returns {@code true} if the given declaration is a candidate for the marker, has no references
     * anywhere in the resource set, and is not exempt.
     */
    public boolean isUnused(RosettaRootElement element) {
        Resource resource = element.eResource();
        if (resource == null || resource.getResourceSet() == null) {
            return false;
        }
        if (!isCandidate(element, resource) || isExempt(element)) {
            return false;
        }
        ElementId id = idOf(element);
        if (id == null) {
            return false;
        }
        return !isReferenced(id, resource.getResourceSet());
    }

    /**
     * Whether the marker applies to this declaration at all: every root element that carries a name does,
     * with two exclusions that are matters of correctness rather than policy.
     *
     * <p>The naming requirement excludes {@code RosettaReport}, the one root element with no name — there
     * would be nothing to attach the marker to.
     *
     * @see #isMetaType
     * @see #isInBuiltinResource
     */
    private boolean isCandidate(RosettaRootElement element, Resource resource) {
        if (isMetaType(element) || isInBuiltinResource(resource)) {
            return false;
        }
        return element instanceof RosettaNamed named && named.getName() != null;
    }

    /**
     * A {@code metaType} is named, but nothing in the grammar ever cross-references one, so the walk below
     * could never see it as used and the marker would be permanent. Meta types are resolved by <em>name</em>
     * against the index instead ({@code RosettaConfigExtension#findMetaTypes}, itself deprecated pending
     * their removal from the model): a model writes {@code [metadata reference]}, whose
     * {@code AnnotationRef.attribute} points at the {@code reference} attribute of the builtin
     * {@code metadata} <em>annotation</em>, never at the {@code metaType reference string} declaration.
     */
    private boolean isMetaType(RosettaRootElement element) {
        return element instanceof RosettaMetaType;
    }

    /**
     * The built-in models are read-only, so a marker on one could never be acted upon, and most of the kinds
     * they declare — basic types, record types, library functions, annotations — cannot carry
     * {@code [suppressUnused]} regardless (the {@code calculation} type alias also declared there now could,
     * but this exclusion is checked first, so it never gets the chance to matter). Every builtin a given
     * model happens not to use would therefore be marked permanently.
     *
     * <p>{@code IncomingReferenceChanges} in {@code rune-ide} skips the same resources, for the related
     * reason that revalidating a resource which can never carry a marker cannot change its diagnostics.
     */
    private boolean isInBuiltinResource(Resource resource) {
        return RosettaBuiltinsService.isBuiltinResource(resource.getURI());
    }

    /**
     * Whether an otherwise unreferenced declaration should be left unmarked anyway.
     *
     * <p>{@code [suppressUnused]} is handled generically for anything {@code Annotated}, which in practice
     * means a function, type, enumeration, type alias or schema. The other candidate kinds have no
     * {@code Annotations} fragment in their grammar rule, so the annotation cannot be written on them and
     * their annotation list is always empty — that is accepted rather than worked around, so those kinds
     * have no opt-out.
     */
    private boolean isExempt(RosettaRootElement element) {
        if (element instanceof Annotated annotated && isAnnotatedWith(annotated, SUPPRESS_UNUSED_ANNOTATION)) {
            return true;
        }
        if (element instanceof Function function) {
            return isExemptFunction(function);
        }
        // A root type is an entry point by definition: it is what a model exposes to be built.
        return element instanceof Data data && isAnnotatedWith(data, ROOT_TYPE_ANNOTATION);
    }

    private boolean isExemptFunction(Function function) {
        // Function extensions are not independent entry points.
        if (function.getSuperFunction() != null) {
            return true;
        }
        // Transform functions (ingest/projection) are entry points called from outside the model.
        if (!function.getTransform().isEmpty()) {
            return true;
        }
        // Functions with no body are reported separately (codeImplementation check); ignore here.
        return function.getOutput() != null
                && function.getOutput().getName() != null
                && function.getOperations().isEmpty();
    }

    private boolean isAnnotatedWith(Annotated annotated, String annotationName) {
        return annotated.getAnnotations().stream()
                .map(AnnotationRef::getAnnotation)
                .filter(Objects::nonNull)
                .anyMatch(annotation -> annotationName.equals(annotation.getName()));
    }

    private boolean isReferenced(ElementId candidate, ResourceSet resourceSet) {
        for (Resource resource : resourceSet.getResources()) {
            if (outgoingReferences(resource).contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private Set<ElementId> outgoingReferences(Resource resource) {
        if (resource.getContents().isEmpty()) {
            return Set.of();
        }
        return cache.get(OUTGOING_REFERENCES_CACHE_KEY, resource, () -> computeOutgoingReferences(resource));
    }

    /**
     * Collects every declaration referenced from the given resource, by walking its live AST and following
     * each object's cross-references.
     *
     * <p>The cross-reference index is not a viable alternative here, even though it does contain these
     * references: {@code createReferenceDescriptions} is not overridden by
     * {@code RosettaResourceDescriptionStrategy}, so it descends into expressions and records calls just
     * like any other cross-reference. What rules it out is that
     * {@code DefaultResourceDescriptionStrategy.isResolvedAndExternal} only records references that cross a
     * resource boundary, so same-file references are invisible to it; and {@code IResourceDescriptions}
     * exposes no target-to-source reverse lookup, so answering "is this declaration referenced anywhere"
     * would still require scanning every resource's outgoing references. The live AST walk sees both
     * same-file and cross-file references uniformly with one mechanism.
     *
     * <p>Two properties make a single generic walk correct for every reference shape in the grammar, so
     * that a new grammar rule cannot silently reintroduce a false positive:
     *
     * <ul>
     * <li><b>Container rollup.</b> Each target is attributed to the root element declaring it, so a
     * reference to an enum <em>value</em> (a {@code schema} format, a {@code switch} case guard) counts as
     * a use of its enumeration, and a reference to an attribute counts as a use of its type.</li>
     * <li><b>Self-reference exclusion.</b> A reference whose target is declared by the same root element as
     * its source does not count, so {@code type Foo: child Foo (0..1)} and a self-recursive function are
     * still flagged.</li>
     * </ul>
     */
    private Set<ElementId> computeOutgoingReferences(Resource resource) {
        Set<ElementId> referenced = new HashSet<>();
        for (TreeIterator<EObject> it = resource.getAllContents(); it.hasNext();) {
            EObject source = it.next();
            RosettaRootElement sourceRoot = null;
            boolean sourceRootResolved = false;
            for (EObject target : source.eCrossReferences()) {
                if (target.eIsProxy()) {
                    continue;
                }
                RosettaRootElement targetRoot = declaringRootElement(target);
                if (targetRoot == null) {
                    continue;
                }
                if (!sourceRootResolved) {
                    sourceRoot = declaringRootElement(source);
                    sourceRootResolved = true;
                }
                if (targetRoot == sourceRoot) {
                    continue;
                }
                ElementId id = idOf(targetRoot);
                if (id != null) {
                    referenced.add(id);
                }
            }
        }
        return referenced;
    }

    private static RosettaRootElement declaringRootElement(EObject object) {
        for (EObject current = object; current != null; current = current.eContainer()) {
            if (current instanceof RosettaRootElement rootElement) {
                return rootElement;
            }
        }
        return null;
    }

    private ElementId idOf(RosettaRootElement element) {
        QualifiedName qualifiedName = qualifiedNameProvider.getFullyQualifiedName(element);
        return qualifiedName == null ? null : new ElementId(qualifiedName, element.eClass().getName());
    }
}
