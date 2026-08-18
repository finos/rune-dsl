/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.server.diagnostics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.validation.Issue;

import jakarta.inject.Singleton;

/**
 * What the client has been told about each resource, split into the issues the build published for it and the
 * workspace-derived diagnostics appended to those, per provider.
 *
 * <p>Recording both halves is what makes a diff possible. LSP {@code publishDiagnostics} replaces a
 * resource's diagnostics wholesale, so amending the derived half means republishing the base half with it;
 * and only resources whose derived half actually changed need republishing at all.
 *
 * <p>The base half is taken from what was published rather than recomputed, because not every diagnostic
 * comes from the validator — a code generation failure is reported straight to the acceptor, and
 * regenerating the list from validation alone would drop it.
 *
 * <p>URIs are absolute, so one store can serve every project.
 */
@Singleton
public class DerivedDiagnosticsStore {
    /**
     * The identity of a diagnostic for comparison purposes. Needed because Xtext's {@link Issue}
     * implementations do not implement {@code equals}, so the lists themselves cannot be compared: an
     * identity comparison would report every resource as changed on every build.
     */
    private record DiagnosticValue(String message, String issueCode, Integer offset, Integer length) {
        private static DiagnosticValue of(Issue issue) {
            return new DiagnosticValue(issue.getMessage(), issue.getCode(), issue.getOffset(), issue.getLength());
        }
    }

    /**
     * The {@link Issue} objects are retained rather than reduced to the values above, because republishing
     * needs the line and column information they carry. They hold no reference to the model.
     */
    private record Entry(List<Issue> base, Map<Class<?>, List<Issue>> derived) {
    }

    private final Map<URI, Entry> entries = new HashMap<>();

    /**
     * Records the issues just published for a resource by something other than a sweep.
     */
    public void recordBase(URI uri, Iterable<Issue> base) {
        List<Issue> copy = new ArrayList<>();
        base.forEach(copy::add);
        entries.merge(uri, new Entry(List.copyOf(copy), Map.of()),
                (existing, added) -> new Entry(added.base(), existing.derived()));
    }

    /**
     * Whether anything has been published for this resource yet. A resource the server has never reported on
     * — a dependency loaded only to resolve references, say — is not one to start reporting on here.
     */
    public boolean isPublished(URI uri) {
        return entries.containsKey(uri);
    }

    public List<Issue> baseOf(URI uri) {
        Entry entry = entries.get(uri);
        return entry == null ? List.of() : entry.base();
    }

    /**
     * Every provider's derived diagnostics currently published for this resource, in provider order.
     */
    public List<Issue> derivedOf(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return List.of();
        }
        List<Issue> result = new ArrayList<>();
        entry.derived().values().forEach(result::addAll);
        return result;
    }

    /**
     * The resources currently carrying derived diagnostics, i.e. exactly those a sweep has amended.
     */
    public Set<URI> urisWithDerivedDiagnostics() {
        return entries.entrySet().stream()
                .filter(entry -> !entry.getValue().derived().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Whether the given per-provider diagnostics differ from what is recorded for this resource, i.e. whether
     * the client's view of it is out of date.
     */
    public boolean derivedDiffers(URI uri, Map<Class<?>, List<Issue>> derived) {
        Entry entry = entries.get(uri);
        return !comparable(derived).equals(comparable(entry == null ? Map.of() : entry.derived()));
    }

    /**
     * Records the derived diagnostics just published for a resource. Call only <em>after</em> the publish
     * succeeds, so that a sweep cut short leaves the resources it did not reach recorded as they were and the
     * next one repairs them.
     */
    public void recordDerived(URI uri, Map<Class<?>, List<Issue>> derived) {
        entries.merge(uri, new Entry(List.of(), contributions(derived, List::copyOf)),
                (existing, added) -> new Entry(existing.base(), added.derived()));
    }

    public void remove(URI uri) {
        entries.remove(uri);
    }

    public void clear() {
        entries.clear();
    }

    /**
     * The comparable form of a per-provider map, so that lists of {@link Issue} — which have no
     * {@code equals} — can be diffed.
     */
    private static Map<Class<?>, List<DiagnosticValue>> comparable(Map<Class<?>, List<Issue>> derived) {
        return contributions(derived, issues -> issues.stream().map(DiagnosticValue::of).toList());
    }

    /**
     * A per-provider map with the providers contributing nothing dropped, so that "no entry" and "an empty
     * contribution" are the same thing both to record and to compare.
     */
    private static <T> Map<Class<?>, List<T>> contributions(Map<Class<?>, List<Issue>> derived,
            Function<List<Issue>, List<T>> mapper) {
        Map<Class<?>, List<T>> result = new LinkedHashMap<>();
        derived.forEach((provider, issues) -> {
            if (!issues.isEmpty()) {
                result.put(provider, mapper.apply(issues));
            }
        });
        return result;
    }
}
