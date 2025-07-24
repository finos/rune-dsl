package com.regnosys.rosetta.ide.hover

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import org.junit.jupiter.api.Test

class RosettaDocumentationProviderTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testMultiCardinalityDocs() {
		testHover[
			val model = '''
			namespace foo.bar
			
			type Foo:
				attr int (2..3)
			'''
			it.model = model
			it.line = 3
			it.column = 1
			it.assertHover = [
				val expected = '''
				[[3, 1] .. [3, 5]]
				kind: markdown
				value: **Multi cardinality.**
				'''
				assertEquals(expected, toExpectation)
			]
		]
	}
	
	@Test
	def testImplicitEnumDocs() {
		testHover[
			val model = '''
			namespace foo.bar
			
			enum MyEnum:
				VALUE
			
			func Foo:
				output:
					result MyEnum (1..1)
				
				set result:
					VALUE
			'''
			it.model = model
			it.line = 10
			it.column = 2
			it.assertHover = [
				val expected = '''
				[[10, 2] .. [10, 7]]
				kind: markdown
				value: MyEnum
				'''
				assertEquals(expected, toExpectation)
			]
		]
	}
}