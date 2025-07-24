package com.regnosys.rosetta.ide.tests

import java.util.Map
import org.junit.jupiter.api.BeforeEach
import org.eclipse.xtext.testing.TextDocumentConfiguration
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.Position
import java.util.List
import org.eclipse.lsp4j.Diagnostic

abstract class AbstractRosettaLanguageServerValidationTest extends AbstractRosettaLanguageServerTest {
	Map<String, Integer> versionMap
	
	@BeforeEach
	def void beforeEach() {
		versionMap = newHashMap
		
		initializeContext(new TextDocumentConfiguration)
	}
	
	protected def String createModel(String fileName, String model) {
		val uri = fileName.writeFile(model)
		open(uri, model)
		versionMap.put(uri, 1)
		return uri
	}
	protected def void makeChange(String uri, int line, int character, String oldText, String newText) {
		val newVersion = versionMap.get(uri) + 1
		languageServer.didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(uri, newVersion),
                #[
                	new TextDocumentContentChangeEvent(
                		new Range(new Position(line, character), new Position(line, character + oldText.length)),
                        newText
                    )
                ]
        ));
        versionMap.put(uri, newVersion)
	}
	
	protected def void assertIssues(String expected, List<Diagnostic> actual) {
		assertEquals(
			expected,
			actual.toExpectation
		)
	}
	
	protected def dispatch String toExpectation(Diagnostic it) {
		'''«severity» «range.toExpectation»: «message»'''
	}
}
