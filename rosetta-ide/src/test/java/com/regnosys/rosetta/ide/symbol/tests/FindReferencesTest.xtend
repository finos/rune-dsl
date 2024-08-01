package com.regnosys.rosetta.ide.symbol.tests

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest

class FindReferencesTest extends AbstractRosettaLanguageServerTest {	
	@Test
	def testFindTranslateReferences() {
		testReferences[
			val model = '''
				namespace foo.bar
				
				type Foo:
					a string (1..1)
				
				type Bar:
					b Qux (1..1)
				
				type Qux:
					c string (1..1)
				
				translate source FooBar {
					translate Bar to Foo {
						a: translate b, 42 to string
					}
				
					translate qux Qux, context number to string:
						qux -> c + context to-string
				}
			'''
			it.filesInScope = #{
				"OtherModel.rosetta" -> '''
					namespace foo.bar.other
					
					import foo.bar.*
					
					func DoTheThing:
						output:
							result string (1..1)
						set result:
							translate Qux { c: "value" }, 42 to string using FooBar
					'''
			}
			it.model = model
			it.line = 16
			it.column = 1
			it.expectedReferences = '''
				OtherModel.rosetta [[8, 2] .. [8, 11]]
				MyModel.rosetta [[13, 5] .. [13, 14]]
				'''
		]
	}
}
