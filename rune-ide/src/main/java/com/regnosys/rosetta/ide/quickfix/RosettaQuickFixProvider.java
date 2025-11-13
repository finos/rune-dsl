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

package com.regnosys.rosetta.ide.quickfix;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;

import java.util.List;

import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.ide.editor.quickfix.AbstractDeclarativeIdeQuickfixProvider;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.editor.quickfix.ISemanticModification;
import org.eclipse.xtext.ide.editor.quickfix.QuickFix;
import org.eclipse.xtext.ide.server.Document;

import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.ide.util.RangeUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.utils.ConstructorManagementService;
import com.regnosys.rosetta.utils.ImportManagementService;
import com.regnosys.rosetta.validation.RosettaIssueCodes;

public class RosettaQuickFixProvider extends AbstractDeclarativeIdeQuickfixProvider {
	@Inject
	private RangeUtils rangeUtils;
	@Inject
	private ImportManagementService importManagementService;
	@Inject 
	private CodeActionUtils codeActionUtils;
	@Inject
	private ConstructorManagementService constructorManagementService;

	@QuickFix(RosettaIssueCodes.REDUNDANT_SQUARE_BRACKETS)
	public void fixRedundantSquareBrackets(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Remove square brackets", (Diagnostic diagnostic, EObject object, Document document) -> {
			Range range = rangeUtils.getRange(object);
			String original = document.getSubstring(range);
			String edited = original.replaceAll("^\\[ +|\\s+\\]$", "");
			return createTextEdit(diagnostic, edited);
		});
	}

	@QuickFix(RosettaIssueCodes.MANDATORY_SQUARE_BRACKETS)
	public void fixMandatorySquareBrackets(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Add square brackets", (Diagnostic diagnostic, EObject object, Document document) -> {
			Range range = rangeUtils.getRange(object);
			String original = document.getSubstring(range);
			String edited = "[ " + original + " ]";
			return createTextEdit(diagnostic, edited);
		});
	}

	@QuickFix(RosettaIssueCodes.MANDATORY_THEN)
	public void fixMandatoryThen(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Add `then`", (Diagnostic diagnostic, EObject object, Document document) -> {
			RosettaUnaryOperation op = (RosettaUnaryOperation) object;
			Range range = rangeUtils.getRange(op, ROSETTA_OPERATION__OPERATOR);
			String original = document.getSubstring(range);
			String edited = "then " + original;
			TextEdit edit = new TextEdit(range, edited);
			return List.of(edit);
		});
	}

	@QuickFix(RosettaIssueCodes.UNUSED_IMPORT)
	@QuickFix(RosettaIssueCodes.DUPLICATE_IMPORT)
	public void fixUnoptimizedImports(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Optimize imports", (Diagnostic diagnostic, EObject object, Document document) -> {
			Import importObj = (Import) object;
			EObject container = importObj.eContainer();

			if (container instanceof RosettaModel) {
				RosettaModel model = (RosettaModel) container;
				List<Import> imports = model.getImports();

				Range importsRange = codeActionUtils.getImportsRange(imports);

				importManagementService.cleanupImports(model);
				String sortedImportsText = importManagementService.toString(imports);

				return List.of(new TextEdit(importsRange, sortedImportsText));
			}

			// if not model, return empty list of edits
			return List.of();

		});
	}

    @QuickFix(RosettaIssueCodes.MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT)
    public void missingAttributes(DiagnosticResolutionAcceptor acceptor) {
        ISemanticModification semanticModification = (Diagnostic diagnostic, EObject object) ->
                context -> constructorManagementService.modifyConstructorWithRequiredAttributes(getContainerOfType(object, RosettaConstructorExpression.class));
        acceptor.accept("Add mandatory attributes", semanticModification);
    }

    @QuickFix(RosettaIssueCodes.MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT)
    public void addAllMissingAttributes(DiagnosticResolutionAcceptor acceptor) {
        ISemanticModification semanticModification = (Diagnostic diagnostic, EObject object) ->
                context -> constructorManagementService.modifyConstructorWithAllAttributes(getContainerOfType(object, RosettaConstructorExpression.class));
        acceptor.accept("Add all attributes", semanticModification);
    }

    @QuickFix(RosettaIssueCodes.CHANGED_EXTENDED_FUNCTION_PARAMETERS)
    public void copyOriginalInputsAndOutput(DiagnosticResolutionAcceptor acceptor) {
        ISemanticModification semanticModification = (Diagnostic diagnostic, EObject object) -> 
                context -> {
                    Function func = getContainerOfType(object, Function.class);
                    if (func == null) return;
                    Function original = func.getSuperFunction();
                    if (original == null) return;
                    
                    func.getInputs().clear();
                    for (Attribute input : original.getInputs()) {
                        func.getInputs().add(EcoreUtil2.copy(input));
                    }
                    func.setOutput(EcoreUtil2.copy(original.getOutput()));
                };
        acceptor.accept("Copy original inputs and output", semanticModification);
    }
}
