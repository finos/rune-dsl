package com.regnosys.rosetta.tests.validation;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Assertions;

public class RosettaValidationTestHelper extends ValidationTestHelper {
	public RosettaValidationTestHelper() {
		super(Mode.EXACT);
	}
	
	public void assertIssues(EObject model, String issuesExpectation) {
		List<Issue> issues = validate(model);
		
		String issuesRepresentation = getIssuesAsString(model, issues, new StringBuilder()).toString();
		
		Assertions.assertEquals(issuesExpectation, issuesRepresentation);
	}
}
