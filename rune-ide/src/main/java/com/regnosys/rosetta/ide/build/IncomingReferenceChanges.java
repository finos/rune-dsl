package com.regnosys.rosetta.ide.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaPackage;

/**
 * Given the deltas a build produced, works out which <em>other</em> resources declare an element whose set
 * of incoming cross-references changed.
 *
 * <p>Needed because a marker like "this function is never used" is a statement about a declaration in one
 * file that depends entirely on the contents of other files. Xtext only revalidates resources it considers
 * affected, and its notion of "affected" is one-directional: a candidate is affected when it references
 * something that changed. The reverse — the declaring file must be revalidated because a <em>call site</em>
 * elsewhere appeared or disappeared — has no equivalent, so the marker goes stale (see
 * {@code UnusedElementStalenessTest}).
 *
 * <p><b>Why this is computed after the build rather than in {@code IResourceDescription.Manager#isAffected}.</b>
 * Overriding {@code isAffected} is the natural place and cannot work. At the point
 * {@code Indexer#computeAndIndexAffected} asks it, the <em>new</em> side of a delta is a
 * {@code ResolvedResourceDescription}, whose {@code getReferenceDescriptions()} and
 * {@code getImportedNames()} deliberately return nothing and log an {@code IllegalStateException}. Nor can
 * they be made available there: both are derived by resolving cross-references
 * ({@code EcoreUtil2.resolveLazyCrossReferences}), and {@code Indexer} computes descriptions with
 * {@code CompilerPhases#setIndexing} enabled and <em>before</em> registering them in the new index — so
 * resolving at that point runs against an index that does not yet contain the model and permanently
 * records bogus "couldn't resolve reference" errors on the resources. Only the <em>old</em> side of a delta
 * carries references (it is a {@code SerializableResourceDescription} read back from the index), which is
 * enough to spot a reference disappearing but never one appearing.
 *
 * <p>After the build both sides are full {@code SerializableResourceDescription}s — see
 * {@code IncrementalBuilder.InternalStatefulIncrementalBuilder#launch}, which resolves cross-references and
 * then stores {@code SerializableResourceDescription.createCopy(description)} in the index — and
 * {@code createCopy} copies reference descriptions. So the diff below is exact in both directions.
 *
 * <p>Reference descriptions only ever record references that cross a resource boundary
 * ({@code DefaultResourceDescriptionStrategy#isResolvedAndExternal}), so this never reports a resource
 * because of a change within itself.
 */
final class IncomingReferenceChanges {
    private IncomingReferenceChanges() {
    }

    /**
     * The resources that need revalidating because one of the given deltas added or removed a reference to
     * an element they declare. Resources in {@code alreadyBuilt} are left out, since the build has just
     * revalidated them anyway.
     *
     * <p>{@code index} is the build's resulting index, used to establish what kind of declaration each
     * changed reference points at so that references which can never move a marker are ignored — see
     * {@link #markerCapableFragments}. Without that, this pass fires for every changed cross-resource
     * reference of any kind, and since references between types are the norm in a real model rather than
     * the exception, widening the marker beyond functions would also widen this trigger to most edits.
     */
    static Set<URI> resourcesToRevalidate(
            Collection<IResourceDescription.Delta> deltas,
            Set<URI> alreadyBuilt,
            IResourceDescriptions index) {
        Set<URI> result = new LinkedHashSet<>();
        Map<URI, Set<String>> markerCapableFragmentsByResource = new HashMap<>();
        for (IResourceDescription.Delta delta : deltas) {
            Map<URI, Set<URI>> before = targetsByDeclaringResource(delta.getOld());
            Map<URI, Set<URI>> after = targetsByDeclaringResource(delta.getNew());
            if (before.equals(after)) {
                continue;
            }
            Set<URI> candidates = new LinkedHashSet<>(before.keySet());
            candidates.addAll(after.keySet());
            for (URI candidate : candidates) {
                if (alreadyBuilt.contains(candidate) || result.contains(candidate)) {
                    continue;
                }
                Set<String> markerCapable = markerCapableFragmentsByResource.computeIfAbsent(
                        candidate, uri -> markerCapableFragments(index.getResourceDescription(uri)));
                if (markerCapable.isEmpty()) {
                    // Nothing this resource declares can carry a marker (e.g. the builtin annotations), so
                    // revalidating it could not change its diagnostics.
                    continue;
                }
                Set<URI> relevantBefore = retainMarkerRelevant(targetsIn(before, candidate), markerCapable);
                Set<URI> relevantAfter = retainMarkerRelevant(targetsIn(after, candidate), markerCapable);
                if (!relevantBefore.equals(relevantAfter)) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }

    /**
     * The elements the given resource references, grouped by the resource declaring them. Grouping by
     * declaring resource is what makes the diff answer "did the references into <em>that file</em> change",
     * and keeping the individual element URIs means switching a call from one declaration to another in the
     * same file still counts as a change to both.
     */
    private static Map<URI, Set<URI>> targetsByDeclaringResource(IResourceDescription description) {
        if (description == null) {
            return Map.of();
        }
        Map<URI, Set<URI>> result = new HashMap<>();
        for (IReferenceDescription reference : description.getReferenceDescriptions()) {
            URI target = reference.getTargetEObjectUri();
            if (target != null) {
                result.computeIfAbsent(target.trimFragment(), uri -> new HashSet<>()).add(target);
            }
        }
        return result;
    }

    private static Set<URI> targetsIn(Map<URI, Set<URI>> targetsByResource, URI resource) {
        return targetsByResource.getOrDefault(resource, Set.of());
    }

    /**
     * The URI fragments of the marker-capable declarations the given resource exports. Empty when the
     * description is missing, which happens only for a resource the build deleted — one there is no point
     * revalidating either way — and empty for the built-in models, which are read-only and which
     * {@code UnusedElementHelper} therefore never marks.
     */
    private static Set<String> markerCapableFragments(IResourceDescription description) {
        if (description == null || RosettaBuiltinsService.isBuiltinResource(description.getURI())) {
            return Set.of();
        }
        Set<String> fragments = new HashSet<>();
        for (IEObjectDescription exported : description.getExportedObjects()) {
            if (!isMarkerCapable(exported.getEClass())) {
                continue;
            }
            String fragment = exported.getEObjectURI().fragment();
            if (fragment != null) {
                fragments.add(fragment);
            }
        }
        return fragments;
    }

    /**
     * Mirrors {@code UnusedElementHelper#isCandidate}: a named root element, excluding {@code metaType}.
     *
     * <p>Both conditions are needed. Requiring a name excludes {@code RosettaReport}; requiring a root
     * element excludes the named things <em>nested</em> inside declarations — {@code RosettaFeature} extends
     * {@code RosettaNamed}, so attributes, enum values and record features are all named too, and treating
     * them as marker-capable in their own right would defeat the pruning below. They are still accounted for,
     * as containment paths under the declaration that owns them; see {@link #retainMarkerRelevant}.
     */
    private static boolean isMarkerCapable(EClass eClass) {
        return RosettaPackage.Literals.ROSETTA_ROOT_ELEMENT.isSuperTypeOf(eClass)
                && RosettaPackage.Literals.ROSETTA_NAMED.isSuperTypeOf(eClass)
                && !RosettaPackage.Literals.ROSETTA_META_TYPE.isSuperTypeOf(eClass);
    }

    /**
     * Keeps only the targets that could move a marker: a marker-capable declaration itself, or something
     * nested inside one. The nested case matters because {@code UnusedElementHelper} attributes a reference
     * to the declaration containing its target, so a reference to an attribute counts as a use of its type
     * and a reference to an enum value as a use of its enumeration.
     *
     * <p>Containment is read off the URI fragment, whose EMF form is a path of containment steps
     * ({@code //@elements.3/@attributes.1}) — no {@code IFragmentProvider} is bound, so this holds. The
     * trailing separator is what stops {@code //@elements.3} from matching {@code //@elements.30}.
     */
    private static Set<URI> retainMarkerRelevant(Set<URI> targets, Set<String> markerCapableFragments) {
        Set<URI> relevant = new HashSet<>();
        for (URI target : targets) {
            if (isDeclaredByMarkerCapable(target.fragment(), markerCapableFragments)) {
                relevant.add(target);
            }
        }
        return relevant;
    }

    /** Walks up the containment path, so the cost is the nesting depth rather than the number of exports. */
    private static boolean isDeclaredByMarkerCapable(String fragment, Set<String> markerCapableFragments) {
        for (String current = fragment; current != null; current = containerFragment(current)) {
            if (markerCapableFragments.contains(current)) {
                return true;
            }
        }
        return false;
    }

    private static String containerFragment(String fragment) {
        int lastStep = fragment.lastIndexOf('/');
        return lastStep <= 0 ? null : fragment.substring(0, lastStep);
    }
}
