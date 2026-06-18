package com.regnosys.rosetta.ide.server;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.xtext.testing.TextDocumentConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HandleParseErrorGracefullyTest extends AbstractRosettaLanguageServerTest {
	@Test
	void testStackoverflowResults() {
		// Create a model containing 1000 `else if`'s, which should cause
		// a stack overflow during parsing.
		StringBuilder model = new StringBuilder("""
				namespace "test"
				version "test"

				func Foo:
					output: result int (0..1)
					set result:
						if False then 42
				""");
		for (int i = 0; i <= 1000; i++) {
			model.append("\t\t\telse if False then 42\n");
		}

		TextDocumentConfiguration configuration = new TextDocumentConfiguration();
		configuration.setFilePath("MyModel." + getFileExtension());
		configuration.setModel(model.toString());
		String uri = initializeContext(configuration).getUri();

		List<Diagnostic> issues = getDiagnostics().get(uri);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals("An unexpected parse error occured.", issues.get(0).getMessage());
	}
}
