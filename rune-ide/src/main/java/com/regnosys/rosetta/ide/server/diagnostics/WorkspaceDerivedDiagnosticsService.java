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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ide.server.ProjectManager;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Publishes diagnostics that depend on more of the workspace than the resource they are reported on, by
 * recomputing them for every resource after each build and publishing the ones that changed.
 *
 * <p>Computing such a diagnostic inside validation cannot work: the answer for file A changes when file B is
 * edited, and the language server only revalidates resources that <em>reference</em> what changed, never
 * those referenced <em>by</em> it. Recomputing from the settled workspace replaces guessing whose answer
 * changed with cheaply computing them all and diffing the results. These diagnostics are then no part of any
 * cached validation answer, so a republish needs nothing unloaded or revalidated.
 *
 * <p>The sweep is per project resource set. In the supported deployment that is a single project holding
 * every workspace folder as a source folder, so a declaration in one folder referenced from another is in
 * the same resource set and is seen. Workspaces with genuinely separate projects are not supported.
 *
 * <p>Caveat inherited from the build itself: if clustering ever unloads resources mid-sweep, a provider
 * reading the live model cannot see them. Not reachable at the scale this has been measured at.
 */
@Singleton
public class WorkspaceDerivedDiagnosticsService {
    private static final Key<Set<IWorkspaceDerivedDiagnosticsProvider>> PROVIDERS_KEY =
            Key.get(new TypeLiteral<Set<IWorkspaceDerivedDiagnosticsProvider>>() {
            });

    @Inject
    private DerivedDiagnosticsStore store;
    @Inject
    private IResourceServiceProvider.Registry languagesRegistry;
    @Inject
    private OperationCanceledManager operationCanceledManager;

    /**
     * The acceptor the sweep publishes through, captured unwrapped so that a republish carries exactly the
     * list assembled for it.
     */
    private Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor;

    /**
     * Takes over the workspace's issue acceptor, returning the one to publish through from now on.
     *
     * <p>The returned acceptor appends the derived diagnostics currently recorded for a URI to whatever is
     * published for it, so that the build's own publishes — which no longer carry them, since no validator
     * computes them — do not blink the markers off between the build and the sweep that follows it. LSP
     * {@code publishDiagnostics} replaces a resource's diagnostics wholesale, so every publish must carry
     * them. It also records what was published as the base a later republish has to carry.
     */
    public Procedure2<? super URI, ? super Iterable<Issue>> install(
            Procedure2<? super URI, ? super Iterable<Issue>> delegate) {
        this.issueAcceptor = delegate;
        store.clear();
        return (uri, issues) -> {
            store.recordBase(uri, issues);
            delegate.apply(uri, withRecordedDiagnostics(uri, issues));
        };
    }

    /**
     * Recomputes and republishes after a build. Runs in the build's own write request, so the index and every
     * built resource have settled.
     */
    public void afterBuild(List<ProjectManager> projectManagers, List<IResourceDescription.Delta> deltas,
            CancelIndicator cancelIndicator) {
        if (issueAcceptor == null) {
            return;
        }
        deltas.stream().filter(delta -> delta.getNew() == null).forEach(delta -> store.remove(delta.getUri()));
        for (ProjectManager projectManager : projectManagers) {
            // An index-only project is deliberately never validated, so it must not be published for either.
            if (projectManager.getProjectConfig().isIndexOnly()) {
                continue;
            }
            ResourceSet resourceSet = projectManager.getResourceSet();
            if (resourceSet != null) {
                sweep(resourceSet, cancelIndicator);
            }
        }
    }

    private void sweep(ResourceSet resourceSet, CancelIndicator cancelIndicator) {
        Map<IResourceServiceProvider, List<ProviderSweep>> sweepsByLanguage = new IdentityHashMap<>();
        // Iterate a copy: a provider preparing its state may resolve a reference that demand-loads a
        // resource. Anything loaded that way is swept by the next build.
        for (Resource resource : List.copyOf(resourceSet.getResources())) {
            operationCanceledManager.checkCanceled(cancelIndicator);
            IResourceServiceProvider language = languageOf(resource);
            if (language == null) {
                continue;
            }
            List<ProviderSweep> sweeps = sweepsByLanguage.computeIfAbsent(language,
                    it -> beginSweeps(it, resourceSet));
            // A resource nothing has published for is not one to start publishing for here.
            if (!sweeps.isEmpty() && store.isPublished(resource.getURI())) {
                republishIfChanged(resource, sweeps);
            }
        }
    }

    private void republishIfChanged(Resource resource, List<ProviderSweep> sweeps) {
        URI uri = resource.getURI();
        Map<Class<?>, List<Issue>> computed = new LinkedHashMap<>();
        for (ProviderSweep sweep : sweeps) {
            computed.put(sweep.providerType(), sweep.sweep().computeDiagnostics(resource));
        }
        if (!store.derivedDiffers(uri, computed)) {
            return;
        }
        // Published straight through the unwrapped acceptor, so the wrapper cannot attach a second copy.
        List<Issue> merged = new ArrayList<>(store.baseOf(uri));
        computed.values().forEach(merged::addAll);
        issueAcceptor.apply(uri, merged);
        // Only now: a sweep cut short leaves the resources it did not reach recorded as they were, so the
        // next completed build finds them unequal and republishes rather than leaving a marker stale.
        store.recordDerived(uri, computed);
    }

    private List<ProviderSweep> beginSweeps(IResourceServiceProvider language, ResourceSet resourceSet) {
        return providersOf(language).stream()
                .map(provider -> new ProviderSweep(provider.getClass(), provider.beginSweep(resourceSet)))
                .toList();
    }

    /**
     * The providers a language contributes, empty if it contributes none. Resolved through the language's own
     * injector because {@link IResourceServiceProvider#get} cannot express a multibound set.
     */
    private Set<IWorkspaceDerivedDiagnosticsProvider> providersOf(IResourceServiceProvider language) {
        Injector languageInjector = language.get(Injector.class);
        if (languageInjector == null) {
            return Set.of();
        }
        Binding<Set<IWorkspaceDerivedDiagnosticsProvider>> binding =
                languageInjector.getExistingBinding(PROVIDERS_KEY);
        return binding == null ? Set.of() : binding.getProvider().get();
    }

    private IResourceServiceProvider languageOf(Resource resource) {
        if (resource instanceof XtextResource xtextResource) {
            return xtextResource.getResourceServiceProvider();
        }
        return languagesRegistry.getResourceServiceProvider(resource.getURI());
    }

    private Iterable<Issue> withRecordedDiagnostics(URI uri, Iterable<Issue> issues) {
        List<Issue> recorded = store.derivedOf(uri);
        if (recorded.isEmpty()) {
            return issues;
        }
        List<Issue> merged = new ArrayList<>();
        issues.forEach(merged::add);
        merged.addAll(recorded);
        return merged;
    }

    /**
     * One provider's prepared sweep, tagged with the provider type the store keys its entries by.
     */
    private record ProviderSweep(Class<?> providerType, IWorkspaceDerivedDiagnosticsProvider.Sweep sweep) {
    }
}
