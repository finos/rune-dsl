package com.regnosys.rosetta.tests.validation;

import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Assertions;

public class RosettaValidationTestHelper extends ValidationTestHelper {
	public RosettaValidationTestHelper() {
		// Make sure validation tests properly check the message
		super(Mode.EXACT);
	}
	
	public void assertIssues(EObject model, String issuesExpectation) {
		List<Issue> issues = validate(model);
        // Issues come in no deterministic order (root cause: Class::getDeclaredMethods is non-deterministic in order)
        // So sort them to have deterministic tests.
        issues.sort(Comparator.comparing(Issue::getOffset)
                .thenComparing(Issue::getMessage));
		
        String issuesRepresentation = getIssuesAsString(model, issues, new StringBuilder()).toString().trim();
		
		Assertions.assertEquals(issuesExpectation.trim(), issuesRepresentation);
		Assertions.assertNotEquals("", issuesRepresentation, "No issues were found. When asserting for issues, the expected issues may not be empty. Use the method ValidationTestHelper#assertNoIssues instead.");
	}
	
	@Override
	protected StringBuilder doGetIssuesAsString(Resource resource, final Iterable<Issue> issues, StringBuilder result) {
		for (Issue issue : issues) {
			URI uri = issue.getUriToProblem();
			result.append(issue.getSeverity());
			result.append(" (");
			result.append(issue.getCode());
			result.append(") '");
			result.append(issue.getMessage());
			result.append("'");
			// Use line number and column instead of offset (as is used in the superclass)
			result.append(" at ").append(issue.getLineNumber()).append(":").append(issue.getColumn()).append(", length ").append(issue.getLength());
			if (uri != null) {
				EObject eObject = resource.getResourceSet().getEObject(uri, true);
				result.append(", on ");
				result.append(eObject.eClass().getName());
			}
			result.append("\n");
		}
		return result;
	}
}
