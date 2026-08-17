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

package com.regnosys.rosetta.ide.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import org.eclipse.xtext.validation.IDiagnosticConverter;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.regnosys.rosetta.ide.server.diagnostics.IWorkspaceDerivedDiagnosticsProvider;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.ide.validation.UnusedElementHelper.UsageSnapshot;
import com.regnosys.rosetta.validation.RosettaIssueCodes;

import jakarta.inject.Inject;

/**
 * Contributes the "unused declaration" marker for every named root element — see {@link UnusedElementHelper}.
 *
 * <p>Whether a declaration is used depends on every other file, so this is a workspace-derived diagnostic
 * rather than a validator check: it is recomputed after each build and never enters the validation issue
 * stream that batch builds and {@code ValidationTestHelper} tests assert against. The issues carry
 * {@link RosettaIssueCodes#UNUSED_DECLARATION}, which {@code RosettaLanguageServerImpl#toDiagnostic} renders
 * as a {@code Hint} with the {@code Unnecessary} tag — a greyed-out declaration.
 */
public class UnusedElementDiagnosticsProvider implements IWorkspaceDerivedDiagnosticsProvider {
    /**
     * How a declaration of a given kind is described in its marker.
     *
     * @param eClass matched with {@code isSuperTypeOf}, so a subclass is covered by its parent's entry —
     *               {@code Choice} reads as a "Type" and {@code FunctionDispatch} as a "Function"
     * @param noun   sentence-initial, since it starts the message
     */
    private record KindDescriptor(EClass eClass, String noun) {
    }

    /**
     * The known declaration kinds. A kind <em>missing</em> from this table still gets a marker, via the
     * fallback in {@link #markerMessageFor} — that is what keeps {@link UnusedElementHelper}'s "every named root
     * element" rule self-maintaining when the grammar gains one, rather than turning it into a crash.
     *
     * <p>No two entries overlap in the type hierarchy, so the iteration order is not significant.
     */
    private static final List<KindDescriptor> KINDS = List.of(
            new KindDescriptor(SimplePackage.Literals.FUNCTION, "Function"),
            new KindDescriptor(SimplePackage.Literals.DATA, "Type"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_ENUMERATION, "Enumeration"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_TYPE_ALIAS, "Type alias"),
            new KindDescriptor(SimplePackage.Literals.ANNOTATION, "Annotation"),
            new KindDescriptor(RosettaPackage.Literals.SCHEMA, "Schema"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_EXTERNAL_RULE_SOURCE, "Rule source"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_BODY, "Body"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_CORPUS, "Corpus"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_SEGMENT, "Segment"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_BASIC_TYPE, "Basic type"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_RECORD_TYPE, "Record type"),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_EXTERNAL_FUNCTION, "Library function"));

    @Inject
    private UnusedElementHelper unusedElementHelper;
    @Inject
    private IDiagnosticConverter diagnosticConverter;

    @Override
    public Pass beginSweep(ResourceSet resourceSet) {
        UsageSnapshot snapshot = unusedElementHelper.snapshot(resourceSet);
        return resource -> computeUnusedElementIssues(resource, snapshot);
    }

    private List<Issue> computeUnusedElementIssues(Resource resource, UsageSnapshot snapshot) {
        List<Issue> result = new ArrayList<>();
        for (EObject content : resource.getContents()) {
            if (!(content instanceof RosettaModel model)) {
                continue;
            }
            // Every candidate is a named root element, so there is no need to descend into the whole model.
            for (RosettaRootElement element : model.getElements()) {
                if (element instanceof RosettaNamed named && unusedElementHelper.isUnused(element, snapshot)) {
                    FeatureBasedDiagnostic diagnostic = new FeatureBasedDiagnostic(
                            Diagnostic.WARNING,
                            markerMessageFor(named),
                            element,
                            RosettaPackage.Literals.ROSETTA_NAMED__NAME,
                            ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
                            CheckType.NORMAL,
                            RosettaIssueCodes.UNUSED_DECLARATION);
                    diagnosticConverter.convertValidatorDiagnostic(diagnostic, result::add);
                }
            }
        }
        return result;
    }

    /**
     * The message of the marker to report for an unused declaration. Every marker carries the same issue
     * code, so the kind of declaration is named here or nowhere.
     *
     * <p>A kind not covered by {@link #KINDS} falls back to a noun derived from its {@code EClass}, so a newly
     * added root element produces a sensible marker rather than an exception. That matters more than it looks:
     * this runs while diagnostics are being computed for publication, where a throw surfaces as
     * <em>missing</em> diagnostics rather than as a visible error.
     *
     * <p>Static and package-private so that {@code UnusedElementMarkerTest} can assert the above over every
     * named root element the model declares, without standing up a language server.
     */
    static String markerMessageFor(RosettaNamed element) {
        String name = element.getName();
        // Rules are the one kind whose message depends on more than its type: the two rule flavours are one
        // class distinguished by a flag, and "not used by any report" says something "never used" does not.
        if (element instanceof RosettaRule rule) {
            return rule.isEligibility()
                    ? "Eligibility rule '" + name + "' is not used by any report"
                    : "Reporting rule '" + name + "' is never used";
        }
        return KINDS.stream()
                .filter(kind -> kind.eClass().isSuperTypeOf(element.eClass()))
                .findFirst()
                .map(KindDescriptor::noun)
                .orElseGet(() -> fallbackNoun(element.eClass()))
                + " '" + name + "' is never used";
    }

    /**
     * Whether this kind is described by a noun someone chose — its own {@link #KINDS} entry, an ancestor's, or
     * the rule branch above — rather than by {@link #fallbackNoun}. Package-private for
     * {@code UnusedElementMarkerTest}, which holds every candidate kind to it.
     */
    static boolean hasChosenNoun(EClass eClass) {
        return RosettaPackage.Literals.ROSETTA_RULE.isSuperTypeOf(eClass)
                || KINDS.stream().anyMatch(kind -> kind.eClass().isSuperTypeOf(eClass));
    }

    /**
     * A readable noun for a declaration kind with no {@link #KINDS} entry, derived from its {@code EClass}
     * name: the {@code Rosetta} prefix the model uses on most classes is dropped and the remaining camel-case
     * words are spaced out, so {@code RosettaMetaType} reads as "Meta type".
     */
    private static String fallbackNoun(EClass eClass) {
        String name = eClass.getName();
        if (name.startsWith("Rosetta")) {
            name = name.substring("Rosetta".length());
        }
        String spaced = name.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        return spaced.isEmpty() ? "Declaration" : spaced.charAt(0) + spaced.substring(1).toLowerCase();
    }
}
