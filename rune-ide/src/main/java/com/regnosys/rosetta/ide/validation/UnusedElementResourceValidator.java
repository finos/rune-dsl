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

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.validation.CachingResourceValidator;
import com.regnosys.rosetta.validation.RosettaIssueCodes;
import com.regnosys.rosetta.validation.UnusedElementHelper;

import jakarta.inject.Inject;

/**
 * Editor-only resource validator that augments the standard validation with "unused declaration"
 * diagnostics for functions, types, enumerations and rules.
 *
 * <p>This is bound exclusively in the IDE injector ({@code RosettaIdeModule}), so it runs in the
 * language server but never in the runtime injector used by {@code ValidationTestHelper}. The
 * resulting issues carry one of {@link RosettaIssueCodes#UNUSED_CODES}, which
 * {@code RosettaLanguageServerImpl#toDiagnostic} renders as a {@code Hint} with the
 * {@code Unnecessary} tag — i.e. a greyed-out declaration — without surfacing as a build/test warning.
 */
public class UnusedElementResourceValidator extends CachingResourceValidator {
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
                            messageFor(element),
                            element,
                            RosettaPackage.Literals.ROSETTA_NAMED__NAME,
                            ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
                            CheckType.NORMAL,
                            issueCodeFor(element));
                    diagnosticConverter.convertValidatorDiagnostic(diagnostic, result::add);
                }
            }
        }
        return result;
    }

    private String messageFor(RosettaRootElement element) {
        if (element instanceof Function function) {
            return "Function '" + function.getName() + "' is never used";
        }
        if (element instanceof Data data) {
            return "Type '" + data.getName() + "' is never used";
        }
        if (element instanceof RosettaEnumeration enumeration) {
            return "Enumeration '" + enumeration.getName() + "' is never used";
        }
        RosettaRule rule = (RosettaRule) element;
        return rule.isEligibility()
                ? "Eligibility rule '" + rule.getName() + "' is not used by any report"
                : "Reporting rule '" + rule.getName() + "' is never used";
    }

    private String issueCodeFor(RosettaRootElement element) {
        if (element instanceof Function) {
            return RosettaIssueCodes.UNUSED_FUNCTION;
        }
        if (element instanceof Data) {
            return RosettaIssueCodes.UNUSED_TYPE;
        }
        if (element instanceof RosettaEnumeration) {
            return RosettaIssueCodes.UNUSED_ENUMERATION;
        }
        return ((RosettaRule) element).isEligibility()
                ? RosettaIssueCodes.UNUSED_ELIGIBILITY_RULE
                : RosettaIssueCodes.UNUSED_REPORTING_RULE;
    }
}
