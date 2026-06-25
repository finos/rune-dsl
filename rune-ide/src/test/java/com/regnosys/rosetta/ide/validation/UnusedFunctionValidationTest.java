package com.regnosys.rosetta.ide.validation;

import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

public class UnusedFunctionValidationTest extends AbstractRosettaLanguageServerValidationTest {

	@Test
	void unusedFunctionIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Unused:
					output: result int (1..1)
					set result: 42
				""");

		// A Hint with the Unnecessary tag is not a "problem" (severity > Warning), so the model is valid.
		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Function 'Unused' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary),
				"Expected the diagnostic to carry the Unnecessary tag so the editor greys it out");
	}

	@Test
	void calledFunctionIsNotMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Used:
					output: result int (1..1)
					set result: 42

				func Caller:
					output: result int (1..1)
					set result: Used()
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		// Only `Caller` is unused; `Used` is called by `Caller`.
		Assertions.assertEquals(1, diagnostics.size());
		Assertions.assertEquals("Function 'Caller' is never used", diagnostics.get(0).getMessage());
	}

	@Test
	void suppressUnusedOptsOut() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Unused:
					[suppressUnused]
					output: result int (1..1)
					set result: 42
				""");

		assertNoIssues();

		Map<String, List<Diagnostic>> diagnostics = getDiagnostics();
		List<Diagnostic> forUri = diagnostics.get(uri);
		Assertions.assertTrue(forUri == null || forUri.isEmpty(),
				"Expected no diagnostics for a function annotated with [suppressUnused]");
	}
}
