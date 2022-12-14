package com.regnosys.rosetta.ide.semantictokens.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import com.regnosys.rosetta.ide.semantictokens.lsp.LSPSemanticTokenTypesEnum

class SemanticTokenTest extends AbstractRosettaLanguageServerTest {
// Example:
//	@Test
//	def testSemanticToken() {
//		testSemanticToken[
//			val model = '''
//			namespace foo.bar
//			
//			type Foo:
//				a int (1..1)
//			
//			func Foo:
//				inputs: foo Foo (1..1)
//				output: result int (1..1)
//				
//				set result:
//					foo -> a
//			'''
//			it.model = model
//			it.assertSemanticTokens = [
//				val first = head
//				assertNotNull(first)
//				assertEquals(LSPSemanticTokenTypesEnum.PARAMETER, first.tokenType)
//				assertEquals(10, first.line)
//				assertEquals(2, first.startChar)
//				assertEquals(3, first.length)
//			]
//		]
//	}
}