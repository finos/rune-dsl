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
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import org.eclipse.xtext.validation.IDiagnosticConverter;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.validation.CachingResourceValidator;
import com.regnosys.rosetta.validation.RosettaIssueCodes;
import com.regnosys.rosetta.validation.UnusedElementHelper;

import jakarta.inject.Inject;

/**
 * Editor-only resource validator that augments the standard validation with "unused declaration"
 * diagnostics for every named root element — see {@link UnusedElementHelper}.
 *
 * <p>This is bound exclusively in the IDE injector ({@code RosettaIdeModule}), so it runs in the
 * language server but never in the runtime injector used by {@code ValidationTestHelper}. The
 * resulting issues carry {@link RosettaIssueCodes#UNUSED_DECLARATION}, which
 * {@code RosettaLanguageServerImpl#toDiagnostic} renders as a {@code Hint} with the
 * {@code Unnecessary} tag — i.e. a greyed-out declaration — without surfacing as a build/test warning.
 */
public class UnusedElementResourceValidator extends CachingResourceValidator {
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
    public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator cancelIndicator)
            throws OperationCanceledError {
        List<Issue> issues = super.validate(resource, mode, cancelIndicator);
        if (!mode.shouldCheck(CheckType.NORMAL)) {
            return issues;
        }
        List<Issue> unusedElementIssues = computeUnusedElementIssues(resource);
        if (unusedElementIssues.isEmpty()) {
            return issues;
        }
        List<Issue> combined = new ArrayList<>(issues);
        combined.addAll(unusedElementIssues);
        return combined;
    }

    private List<Issue> computeUnusedElementIssues(Resource resource) {
        List<Issue> result = new ArrayList<>();
        for (EObject content : resource.getContents()) {
            if (!(content instanceof RosettaModel model)) {
                continue;
            }
            // Every candidate is a root element, so there is no need to descend into the whole model.
            for (RosettaRootElement element : model.getElements()) {
                if (unusedElementHelper.isUnused(element)) {
                    FeatureBasedDiagnostic diagnostic = new FeatureBasedDiagnostic(
                            Diagnostic.WARNING,
                            markerMessageFor(element),
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
     * <p>Every candidate is a {@link RosettaNamed} (that is what makes it a candidate), so reading the name is
     * always safe. A kind not covered by {@link #KINDS} falls back to a noun derived from its {@code EClass},
     * so a newly added root element produces a sensible marker rather than an exception. That matters more
     * than it looks: this runs while diagnostics are being computed for publication, where a throw surfaces as
     * <em>missing</em> diagnostics rather than as a visible error.
     *
     * <p>Static and package-private so that {@code UnusedElementMarkerTest} can assert the above over every
     * named root element the model declares, without standing up a language server.
     */
    static String markerMessageFor(RosettaRootElement element) {
        String name = ((RosettaNamed) element).getName();
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
     * A readable noun for a declaration kind with no {@link #KINDS} entry, derived from its {@code EClass}
     * name: the {@code Rosetta} prefix the model uses on most classes is dropped and the remaining camel-case
     * words are spaced out, so {@code RosettaTypeAlias} would read as "Type alias".
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
