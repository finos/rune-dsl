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
import com.regnosys.rosetta.rosetta.expression.MapOperation;
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
			String edited = original.substring(1, original.length() - 1).replaceAll("^ +|\\s+$", "");
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
	
	@QuickFix(RosettaIssueCodes.DEPRECATED_MAP)
	public void fixDeprecatedMap(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Replace with `extract`.", (Diagnostic diagnostic, EObject object) -> {
			return (EObject context) -> {
				MapOperation op = (MapOperation)context;
				op.setOperator("extract");
			};
		});
	}
}
