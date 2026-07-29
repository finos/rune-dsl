package com.regnosys.rosetta.ide.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;

/**
 * Given the deltas a build produced, works out which <em>other</em> resources declare an element whose set
 * of incoming cross-references changed.
 *
 * <p>Needed because a marker like "this function is never used" is a statement about a declaration in one
 * file that depends entirely on the contents of other files. Xtext only revalidates resources it considers
 * affected, and its notion of "affected" is one-directional: a candidate is affected when it references
 * something that changed. The reverse — the declaring file must be revalidated because a <em>call site</em>
 * elsewhere appeared or disappeared — has no equivalent, so the marker goes stale (see
 * {@code UnusedFunctionStalenessTest}).
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
     */
    static Set<URI> resourcesToRevalidate(Collection<IResourceDescription.Delta> deltas, Set<URI> alreadyBuilt) {
        Set<URI> result = new LinkedHashSet<>();
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
                if (!targetsIn(before, candidate).equals(targetsIn(after, candidate))) {
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
}
