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

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.validation.Issue;

/**
 * Contributes diagnostics that depend on more of the workspace than the resource they are reported on — the
 * kind a validator cannot produce, because whether the diagnostic applies changes when some <em>other</em>
 * file is edited.
 *
 * <p>A language contributes providers by adding them to the {@code Set<IWorkspaceDerivedDiagnosticsProvider>}
 * multibinding in its language module; {@link WorkspaceDerivedDiagnosticsService} runs them once per build
 * and publishes what changed.
 *
 * <p>Two phases because the whole-workspace state a provider needs is provider-specific, and deriving it per
 * resource rather than per sweep is what makes such a check quadratic.
 */
public interface IWorkspaceDerivedDiagnosticsProvider {
    /**
     * Prepares whatever whole-workspace state this provider needs and returns the per-resource step that
     * reads it. Called once per sweep, before any resource is visited.
     *
     * <p>Must not throw, for the same reason {@link Pass#computeDiagnostics} must not: a throw here surfaces
     * as <em>missing</em> diagnostics rather than as a visible error, and this is the phase doing the
     * expensive work.
     */
    Pass beginSweep(ResourceSet resourceSet);

    /**
     * One sweep's worth of prepared state, applied to each resource in turn. Not reused across sweeps.
     */
    interface Pass {
        /**
         * The diagnostics this provider contributes to the given resource, empty if none. Must not throw:
         * these are published to the client, and a throw on that path surfaces as <em>missing</em>
         * diagnostics rather than as a visible error.
         */
        List<Issue> computeDiagnostics(Resource resource);
    }
}
