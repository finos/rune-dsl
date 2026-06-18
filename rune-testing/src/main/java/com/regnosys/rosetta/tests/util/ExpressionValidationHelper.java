package com.regnosys.rosetta.tests.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.validation.IDiagnosticConverter;
import org.eclipse.xtext.validation.Issue;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.validation.AbstractRosettaValidator;
import com.regnosys.rosetta.validation.ReportValidator;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ExpressionValidationHelper {
	@Inject
	private ReportValidator reportValidator; // TODO: replace this with RosettaValidator once old type system has been removed
	@Inject
	@Named(Constants.LANGUAGE_NAME)
	private String languageName;

	@Inject
	private IDiagnosticConverter converter;

	public List<Issue> validateExpression(RosettaExpression expr) {
		BasicDiagnostic diagnostic = new BasicDiagnostic();
		Map<Object, Object> context = Map.of(AbstractRosettaValidator.CURRENT_LANGUAGE_NAME, languageName);
		reportValidator.validate(expr, diagnostic, context);
		List<Issue> issues = new ArrayList<>();
		IAcceptor<Issue> acc = issues::add;
		if (!diagnostic.getChildren().isEmpty()) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				converter.convertValidatorDiagnostic(childDiagnostic, acc);
			}
		} else {
			converter.convertValidatorDiagnostic(diagnostic, acc);
		}
		return issues;
	}

	public void assertNoIssues(RosettaExpression expr) {
		List<Issue> issues = validateExpression(expr);
		if (!issues.isEmpty()) {
			fail("Expected no issues, but got :" + getIssuesAsString(issues, new StringBuilder()));
		}
	}

	public void assertWarning(RosettaExpression expr, String code, String message) {
		assertIssue(Severity.WARNING, expr, code, message);
	}

	public void assertError(RosettaExpression expr, String code, String message) {
		assertIssue(Severity.ERROR, expr, code, message);
	}

	public void assertIssue(Severity severity, RosettaExpression expr, String code, String message) {
		List<Issue> allIssues = validateExpression(expr);
		boolean hasMatch = allIssues.stream().anyMatch(
				it -> code.equals(it.getCode()) && it.getSeverity() == severity && message.equals(it.getMessage()));
		if (!hasMatch) {
			StringBuilder errMessage = new StringBuilder("Expected [")
					.append(severity)
					.append(" (")
					.append(code)
					.append(") '")
					.append(message)
					.append("'] but got\n");
			getIssuesAsString(allIssues, errMessage);
			fail(errMessage.toString());
		}
	}

	protected StringBuilder getIssuesAsString(Iterable<Issue> issues, StringBuilder result) {
		if (!issues.iterator().hasNext()) {
			result.append("(no issues)\n");
		}
		for (Issue issue : issues) {
			result.append(issue.getSeverity());
			result.append(" (");
			result.append(issue.getCode());
			result.append(") '");
			result.append(issue.getMessage());
			result.append("'");
			result.append(", offset " + issue.getOffset() + ", length " + issue.getLength());
			result.append("\n");
		}
		return result;
	}
}
