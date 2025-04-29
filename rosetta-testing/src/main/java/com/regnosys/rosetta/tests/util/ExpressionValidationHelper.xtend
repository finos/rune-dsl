package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.validation.AbstractRosettaValidator
import java.util.List
import org.eclipse.emf.common.util.BasicDiagnostic
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.xtext.Constants
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.util.IAcceptor
import org.eclipse.xtext.validation.IDiagnosticConverter
import org.eclipse.xtext.validation.Issue

import static com.google.common.collect.Iterables.isEmpty
import static org.junit.jupiter.api.Assertions.*
import jakarta.inject.Inject
import jakarta.inject.Named
import com.regnosys.rosetta.validation.ReportValidator

class ExpressionValidationHelper {
	@Inject
	extension ReportValidator // TODO: replace this with RosettaValidator once old type system has been removed
	@Inject@Named(Constants.LANGUAGE_NAME) 
	String languageName;
	
	@Inject IDiagnosticConverter converter
	
	def List<Issue> validateExpression(RosettaExpression expr) {
		val diagnostic = new BasicDiagnostic
		validate(expr, diagnostic, #{AbstractRosettaValidator.CURRENT_LANGUAGE_NAME -> languageName})
		val issues = newArrayList
		val acc = new IAcceptor<Issue> {
			override accept(Issue t) {
				issues.add(t)
			}
		}
		if (!diagnostic.getChildren().isEmpty()) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				converter.convertValidatorDiagnostic(childDiagnostic, acc);
			}
		} else {
			converter.convertValidatorDiagnostic(diagnostic, acc);
		}
		return issues
	}
	
	def void assertNoIssues(RosettaExpression expr) {
		val issues = expr.validateExpression
		if (!isEmpty(issues))
			fail("Expected no issues, but got :" + getIssuesAsString(issues, new StringBuilder()));
	}
	
	def void assertWarning(RosettaExpression expr, String code, String message) {
		assertIssue(Severity.WARNING, expr, code, message);
	}
	
	def void assertError(RosettaExpression expr, String code, String message) {
		assertIssue(Severity.ERROR, expr, code, message);
	}
	
	def void assertIssue(Severity severity, RosettaExpression expr, String code, String message) {
		val allIssues = expr.validateExpression
		val matchingIssues = 
			allIssues.filter[it.code == code && it.severity == severity && it.message == message]
		if (isEmpty(matchingIssues)) {
			val errMessage = new StringBuilder("Expected [")
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
	
	protected def StringBuilder getIssuesAsString(Iterable<Issue> issues, StringBuilder result) {
		if (isEmpty(issues)) {
			result.append("(no issues)\n")
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