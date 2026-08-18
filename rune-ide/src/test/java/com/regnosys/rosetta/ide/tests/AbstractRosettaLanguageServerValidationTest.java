package com.regnosys.rosetta.ide.tests;

import org.eclipse.lsp4j.*;
import org.eclipse.xtext.testing.TextDocumentConfiguration;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractRosettaLanguageServerValidationTest extends AbstractRosettaLanguageServerTest {
	private Map<String, Integer> versionMap;

	@BeforeEach
	public void beforeEach() {
		versionMap = new HashMap<>();
		initializeContext(new TextDocumentConfiguration());
		awaitPendingWork();
	}

	protected String createModel(String fileName, String model) {
		String uri = writeFile(fileName, model);
		open(uri, model);
		awaitPendingWork();
		versionMap.put(uri, 1);
		return uri;
	}

	protected void makeChange(String uri, int line, int character, String oldText, String newText) {
		int newVersion = versionMap.get(uri) + 1;
		var params = new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, newVersion),
				List.of(
						new TextDocumentContentChangeEvent(
								new Range(new Position(line, character), new Position(line, character + oldText.length())),
								newText
						)
				)
		);
		languageServer.didChange(params);
		awaitPendingWork();
		versionMap.put(uri, newVersion);
	}

	/**
	 * Blocks until the server has finished everything queued so far.
	 *
	 * <p>An LSP notification is fire-and-forget, so {@code didOpen} and {@code didChange} return while the
	 * build they trigger is still running — and that build reads files from disk, while {@code writeFile}
	 * leaves a file empty between creating it and writing its contents. Without this barrier the next thing a
	 * test writes races a build that is still reading, and the build sees a file that is empty or not there.
	 * The symptom is a parse error on a file the test never wrote empty, on whichever file lost the race.
	 */
	private void awaitPendingWork() {
		// Requests are queued on a single thread and run in order, so a read submitted now cannot start
		// until everything submitted before it has finished.
		languageServer.getRequestManager().runRead(cancelIndicator -> null).join();
	}

	protected void assertIssues(String expected, List<Diagnostic> actual) {
		assertEquals(expected.stripTrailing(), toExpectation(actual));
	}

	protected String toExpectation(Diagnostic diagnostic) {
		return String.format("%s %s: %s",
				diagnostic.getSeverity(),
				toExpectation(diagnostic.getRange()),
				diagnostic.getMessage()
		);
	}

	protected String toExpectation(List<Diagnostic> diagnostics) {
		return diagnostics.stream()
				.map(this::toExpectation)
				.collect(Collectors.joining("\n"));
	}

	protected String toExpectation(Range range) {
		return String.format("[%d, %d] -> [%d, %d]",
				range.getStart().getLine(),
				range.getStart().getCharacter(),
				range.getEnd().getLine(),
				range.getEnd().getCharacter()
		);
	}
}
