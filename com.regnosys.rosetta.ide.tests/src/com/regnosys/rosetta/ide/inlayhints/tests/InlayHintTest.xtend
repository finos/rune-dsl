package com.regnosys.rosetta.ide.inlayhints.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest

class InlayHintTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testInlayHint() {
		testInlayHint[
			model = '''
			namespace foo.bar
			
			type Foo:
				a int (1..1)
			
			func Foo:
				inputs: foo Foo (1..1)
				output: result int (1..1)
				
				set result:
					foo -> a
			
			<INLAY on '->' back "Foo">
			'''
			assertInlayHints = [
				val first = head
				assertNotNull(first)
				assertEquals("Foo", first.label.getLeft)
				assertEquals(10, first.position.line)
				assertEquals(6, first.position.character)
			]
		]
	}
}