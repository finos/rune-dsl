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
	}

	protected String createModel(String fileName, String model) {
		String uri = writeFile(fileName, model);
		open(uri, model);
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
		versionMap.put(uri, newVersion);
	}

	protected void assertIssues(String expected, List<Diagnostic> actual) {
		assertEquals(expected, toExpectation(actual));
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
