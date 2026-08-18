package com.regnosys.rosetta.ide.validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;

/**
 * The markers must survive a cold start: files already on disk when the server is initialized, which is how
 * a real editor opens a workspace and is the one shape the other tests here cannot produce.
 *
 * <p>They all go through {@code initializeContext}, which initializes the server against an empty directory
 * and only then writes files, so every publish they make happens after the client's {@code initialized}
 * notification. Publishes made <em>before</em> it are held in a queue that runs in reverse when it drains, so
 * a resource the post-build sweep amended would otherwise reach the client oldest-last and keep the
 * unamended answer — the marker would silently never appear until the file was touched.
 */
public class UnusedElementColdStartTest extends AbstractRosettaLanguageServerTest {

	@Test
	void markersSurviveAWorkspaceThatWasAlreadyOnDiskAtStartup() throws IOException {
		writeBuiltins();
		String declURI = writeFile("decl.rosetta", """
				namespace decl

				func F:
					output: r int (1..1)
					set r: 42
				""");

		initialize();

		List<Diagnostic> diagnostics = getDiagnostics().get(declURI);
		Assertions.assertNotNull(diagnostics, "Nothing was published at all for " + declURI);
		Assertions.assertEquals(List.of("Function 'F' is never used"),
				diagnostics.stream()
						.filter(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary))
						.map(Diagnostic::getMessage)
						.toList(),
				"Expected the unused marker on a file that was on disk before the server started, got: "
						+ diagnostics);
	}

	/** {@code initializeContext} does this for every other test; a cold start has to do it for itself. */
	private void writeBuiltins() throws IOException {
		writeFile(builtins.basicTypesURI.path(),
				new String(builtins.basicTypesURL.openStream().readAllBytes(), StandardCharsets.UTF_8));
		writeFile(builtins.annotationsURI.path(),
				new String(builtins.annotationsURL.openStream().readAllBytes(), StandardCharsets.UTF_8));
	}
}
