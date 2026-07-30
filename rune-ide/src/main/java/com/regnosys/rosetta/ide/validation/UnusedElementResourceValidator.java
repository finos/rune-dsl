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
 * resulting issues carry one of {@link RosettaIssueCodes#UNUSED_CODES}, which
 * {@code RosettaLanguageServerImpl#toDiagnostic} renders as a {@code Hint} with the
 * {@code Unnecessary} tag — i.e. a greyed-out declaration — without surfacing as a build/test warning.
 */
public class UnusedElementResourceValidator extends CachingResourceValidator {
    /**
     * How a declaration of a given kind is described in its marker, and which issue code the marker carries.
     *
     * @param eClass matched with {@code isSuperTypeOf}, so a subclass is covered by its parent's entry —
     *               {@code Choice} reads as a "Type" and {@code FunctionDispatch} as a "Function"
     * @param noun   sentence-initial, since it starts the message
     */
    private record KindDescriptor(EClass eClass, String noun, String issueCode) {
    }

    /**
     * The known declaration kinds. A kind <em>missing</em> from this table still gets a marker, via the
     * fallback in {@link #markerFor} — that is what keeps {@link UnusedElementHelper}'s "every named root
     * element" rule self-maintaining when the grammar gains one, rather than turning it into a crash.
     *
     * <p>No two entries overlap in the type hierarchy, so the iteration order is not significant.
     */
    private static final List<KindDescriptor> KINDS = List.of(
            new KindDescriptor(SimplePackage.Literals.FUNCTION, "Function", RosettaIssueCodes.UNUSED_FUNCTION),
            new KindDescriptor(SimplePackage.Literals.DATA, "Type", RosettaIssueCodes.UNUSED_TYPE),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_ENUMERATION, "Enumeration",
                    RosettaIssueCodes.UNUSED_ENUMERATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_TYPE_ALIAS, "Type alias",
                    RosettaIssueCodes.UNUSED_TYPE_ALIAS),
            new KindDescriptor(SimplePackage.Literals.ANNOTATION, "Annotation",
                    RosettaIssueCodes.UNUSED_ANNOTATION),
            new KindDescriptor(RosettaPackage.Literals.SCHEMA, "Schema", RosettaIssueCodes.UNUSED_SCHEMA),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_EXTERNAL_RULE_SOURCE, "Rule source",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_BODY, "Body",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_CORPUS, "Corpus",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_SEGMENT, "Segment",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_BASIC_TYPE, "Basic type",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_RECORD_TYPE, "Record type",
                    RosettaIssueCodes.UNUSED_DECLARATION),
            new KindDescriptor(RosettaPackage.Literals.ROSETTA_EXTERNAL_FUNCTION, "Library function",
                    RosettaIssueCodes.UNUSED_DECLARATION));

    /** A marker's user-facing message and its issue code. Package-private so it can be asserted on. */
    record Marker(String message, String issueCode) {
    }

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
                    Marker marker = markerFor(element);
                    FeatureBasedDiagnostic diagnostic = new FeatureBasedDiagnostic(
                            Diagnostic.WARNING,
                            marker.message(),
                            element,
                            RosettaPackage.Literals.ROSETTA_NAMED__NAME,
                            ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
                            CheckType.NORMAL,
                            marker.issueCode());
                    diagnosticConverter.convertValidatorDiagnostic(diagnostic, result::add);
                }
            }
        }
        return result;
    }

    /**
     * The marker to report for an unused declaration.
     *
     * <p>Every candidate is a {@link RosettaNamed} (that is what makes it a candidate), so reading the name is
     * always safe. A kind not covered by {@link #KINDS} falls back to a noun derived from its {@code EClass}
     * and the generic issue code, so a newly added root element produces a sensible marker rather than an
     * exception. That matters more than it looks: this runs while diagnostics are being computed for
     * publication, where a throw surfaces as <em>missing</em> diagnostics rather than as a visible error.
     *
     * <p>Static and package-private so that {@code UnusedElementMarkerTest} can assert the above over every
     * named root element the model declares, without standing up a language server.
     */
    static Marker markerFor(RosettaRootElement element) {
        String name = ((RosettaNamed) element).getName();
        // Rules are the one kind whose message depends on more than its type: the two rule flavours are one
        // class distinguished by a flag, and "not used by any report" says something "never used" does not.
        if (element instanceof RosettaRule rule) {
            return rule.isEligibility()
                    ? new Marker("Eligibility rule '" + name + "' is not used by any report",
                            RosettaIssueCodes.UNUSED_ELIGIBILITY_RULE)
                    : new Marker("Reporting rule '" + name + "' is never used",
                            RosettaIssueCodes.UNUSED_REPORTING_RULE);
        }
        return KINDS.stream()
                .filter(kind -> kind.eClass().isSuperTypeOf(element.eClass()))
                .findFirst()
                .map(kind -> new Marker(kind.noun() + " '" + name + "' is never used", kind.issueCode()))
                .orElseGet(() -> new Marker(
                        fallbackNoun(element.eClass()) + " '" + name + "' is never used",
                        RosettaIssueCodes.UNUSED_DECLARATION));
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
