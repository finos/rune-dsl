package com.regnosys.rosetta.ide.inlayhints.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest

class InlayHintTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testImplicitFeatureCall() {
		testInlayHint[
			val model = '''
			namespace foo.bar
			
			type Foo:
				a int (1..1)
				condition C:
					a > 42
			'''
			it.model = model
			it.assertInlayHints = [
				val first = head
				assertNotNull(first)
				assertEquals("Foo ->", first.label.getLeft)
				assertEquals(5, first.position.line)
				
				assertEquals(1, size)
			]
		]
	}
	
	@Test
	def testImplicitParameter() {
		testInlayHint[
			val model = '''
			namespace foo.bar
			
			func Foo:
				inputs:
					a int (1..1)
				output:
					b int (1..1)
				set b:
					a + 1
			
			func Bar:
				output:
					result int (0..*)
				set result:
					[0, 1, 2] extract [ Foo ]
			'''
			it.model = model
			it.assertInlayHints = [
				val first = get(0)
				assertEquals("item int", first.label.getLeft)
				assertEquals(14, first.position.line)
				
				val second = get(1)
				assertEquals("int ->", second.label.getLeft)
				assertEquals(14, second.position.line)
				
				val third = get(2)
				assertEquals("int*", third.label.getLeft)
				assertEquals(14, third.position.line)
				
				assertEquals(3, size)
			]
		]
	}
	
	@Test
	def testRuleOutput() {
		testInlayHint[
			val model = '''
			namespace foo.bar
			
			reporting rule Foo:
				return 42
			
			reporting rule Bar:
				return ["", ""]
			'''
			it.model = model
			it.assertInlayHints = [
				val first = get(0)
				assertEquals("output int", first.label.getLeft)
				assertEquals(2, first.position.line)
				
				val second = get(1)
				assertEquals("output string*", second.label.getLeft)
				assertEquals(5, second.position.line)
				
				assertEquals(2, size)
			]
		]
	}
}