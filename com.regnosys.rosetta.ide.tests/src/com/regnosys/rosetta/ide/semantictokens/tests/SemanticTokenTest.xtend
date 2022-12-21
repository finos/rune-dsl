package com.regnosys.rosetta.ide.semantictokens.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenTypesEnum.*

class SemanticTokenTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testTypesAreMarked() {
		testSemanticToken[
			val model = '''
			namespace foo.bar
			
			enum Bar:
				V
			
			type Foo:
				a int (1..1)
				b Bar (1..1)
				c Foo (0..1)
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(3, size)
				get(0) => [
					assertEquals(BASIC_TYPE, tokenType)
					assertEquals(6, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(1) => [
					assertEquals(ENUM, tokenType)
					assertEquals(7, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(2) => [
					assertEquals(TYPE, tokenType)
					assertEquals(8, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
			]
		]
	}
}