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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.editor.quickfix.AbstractDeclarativeIdeQuickfixProvider;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.editor.quickfix.QuickFix;
import org.eclipse.xtext.ide.server.Document;

import com.regnosys.rosetta.ide.util.RangeUtils;
import com.regnosys.rosetta.validation.RosettaIssueCodes;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

public class RosettaQuickFixProvider extends AbstractDeclarativeIdeQuickfixProvider {
	@Inject
	private RangeUtils rangeUtils;
	
	@QuickFix(RosettaIssueCodes.REDUNDANT_SQUARE_BRACKETS)
	public void fixRedundantSquareBrackets(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Remove square brackets.", (Diagnostic diagnostic, EObject object, Document document) -> {
			Range range = rangeUtils.getRange(object);
			String original = document.getSubstring(range);
			String edited = original.replaceAll("^\\[ +|\\s+\\]$", "");
			return createTextEdit(diagnostic, edited);
		});
	}
	
	@QuickFix(RosettaIssueCodes.MANDATORY_SQUARE_BRACKETS)
	public void fixMandatorySquareBrackets(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Add square brackets.", (Diagnostic diagnostic, EObject object, Document document) -> {
			Range range = rangeUtils.getRange(object);
			String original = document.getSubstring(range);
			String edited = "[ " + original + " ]";
			return createTextEdit(diagnostic, edited);
		});
	}
	
	@QuickFix(RosettaIssueCodes.MANDATORY_THEN)
	public void fixMandatoryThen(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Add `then`.", (Diagnostic diagnostic, EObject object, Document document) -> {
			RosettaUnaryOperation op = (RosettaUnaryOperation)object;
			Range range = rangeUtils.getRange(op, ROSETTA_OPERATION__OPERATOR);
			String original = document.getSubstring(range);
			String edited = "then " + original;
			TextEdit edit = new TextEdit(range, edited);
			return List.of(edit);
		});
	}
}
