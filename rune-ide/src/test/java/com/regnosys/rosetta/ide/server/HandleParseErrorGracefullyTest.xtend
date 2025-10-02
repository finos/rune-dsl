package com.regnosys.rosetta.ide.server

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import org.junit.jupiter.api.Test
import org.eclipse.xtext.testing.TextDocumentConfiguration
import static org.junit.jupiter.api.Assertions.*

class HandleParseErrorGracefullyTest extends AbstractRosettaLanguageServerTest {
	@Test
	def void testStackoverflowResults() {
		val uri = initializeContext(new TextDocumentConfiguration => [
			it.filePath = 'MyModel.' + fileExtension
			// Create a model containing 1000 `else if`'s, which should cause
			// a stack overflow during parsing.
			it.model = '''
			namespace "test"
			version "test"
			
			func Foo:
				output: result int (0..1)
				set result:
					if False then 42
					«FOR i: 0..1000»
					else if False then 42
					«ENDFOR»
			'''
		]).uri
		val issues = diagnostics.get(uri)
		assertEquals(1, issues.size)
		assertEquals("An unexpected parse error occured.", issues.head.message)
	}
}