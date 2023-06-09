package com.regnosys.rosetta.ide.inlayhints.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest

class InlayHintTest extends AbstractRosettaLanguageServerTest {	
	@Test
	def testFunctionalOperation() {
		testInlayHint[
			val model = '''
			namespace foo.bar
			
			func Foo:
				inputs:
					a int (1..1)
				output:
					b number (1..1)
				set b:
					a + 1
			
			func Bar:
				output:
					result int (0..*)
				add result:
					[0, 1, 2] extract i [ Foo(i) ]
			'''
			it.model = model
			it.assertNumberOfInlayHints = 1
			it.assertInlayHints = [
				val first = head
				assertEquals("number (0..*)", first.label.getLeft)
				assertEquals(14, first.position.line)
			]
		]
	}
}