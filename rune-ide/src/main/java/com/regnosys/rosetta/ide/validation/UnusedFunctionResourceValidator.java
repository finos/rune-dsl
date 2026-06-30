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
import java.util.Iterator;
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

import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.validation.CachingResourceValidator;
import com.regnosys.rosetta.validation.RosettaIssueCodes;
import com.regnosys.rosetta.validation.UnusedFunctionHelper;

import jakarta.inject.Inject;

/**
 * Editor-only resource validator that augments the standard validation with "unused function"
 * diagnostics.
 *
 * <p>This is bound exclusively in the IDE injector ({@code RosettaIdeModule}), so it runs in the
 * language server but never in the runtime injector used by {@code ValidationTestHelper}. The
 * resulting issues carry {@link RosettaIssueCodes#UNUSED_FUNCTION}, which
 * {@code RosettaLanguageServerImpl#toDiagnostic} renders as a {@code Hint} with the
 * {@code Unnecessary} tag — i.e. a greyed-out function — without surfacing as a build/test warning.
 */
public class UnusedFunctionResourceValidator extends CachingResourceValidator {
    @Inject
    private UnusedFunctionHelper unusedFunctionHelper;
    @Inject
    private IDiagnosticConverter diagnosticConverter;

    @Override
    public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator cancelIndicator)
            throws OperationCanceledError {
        List<Issue> issues = super.validate(resource, mode, cancelIndicator);
        if (!mode.shouldCheck(CheckType.NORMAL)) {
            return issues;
        }
        List<Issue> unusedFunctionIssues = computeUnusedFunctionIssues(resource);
        if (unusedFunctionIssues.isEmpty()) {
            return issues;
        }
        List<Issue> combined = new ArrayList<>(issues);
        combined.addAll(unusedFunctionIssues);
        return combined;
    }

    private List<Issue> computeUnusedFunctionIssues(Resource resource) {
        List<Issue> result = new ArrayList<>();
        for (Iterator<EObject> it = resource.getAllContents(); it.hasNext();) {
            EObject obj = it.next();
            if (obj instanceof Function function && unusedFunctionHelper.isUnused(function)) {
                FeatureBasedDiagnostic diagnostic = new FeatureBasedDiagnostic(
                        Diagnostic.WARNING,
                        "Function '" + function.getName() + "' is never used",
                        function,
                        RosettaPackage.Literals.ROSETTA_NAMED__NAME,
                        ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
                        CheckType.NORMAL,
                        RosettaIssueCodes.UNUSED_FUNCTION);
                diagnosticConverter.convertValidatorDiagnostic(diagnostic, result::add);
            }
        }
        return result;
    }
}
