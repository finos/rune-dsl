package com.regnosys.rosetta.ide.semantictokens.tests

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenTypesEnum.*

class SemanticTokenTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testAttributeTypesAreMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			enum Bar:
				V
			
			type Foo:
				a int (1..1)
				b Bar (1..1)
				c Foo (0..1)
				d date (0..*)
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(4, size)
				get(0) => [ // int
					assertEquals(TYPE_ALIAS, tokenType)
					assertEquals(6, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(1) => [ // Bar
					assertEquals(ENUM, tokenType)
					assertEquals(7, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(2) => [ // Foo
					assertEquals(TYPE, tokenType)
					assertEquals(8, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(3) => [ // date
					assertEquals(RECORD_TYPE, tokenType)
					assertEquals(9, line)
					assertEquals(3, startChar)
					assertEquals(4, length)
				]
			]
		]
	}
	
	@Test
	def testUndefinedTypesAreNotMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			type Foo:
				a int (1..1)
				b Bar (1..1)
				c Foo (0..1)
				d date (0..*)
			'''
			it.model = model
			it.assertNoIssues = false
			it.assertSemanticTokens = [
				assertEquals(3, size)
				get(0) => [ // int
					assertEquals(TYPE_ALIAS, tokenType)
					assertEquals(3, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(1) => [ // Foo
					assertEquals(TYPE, tokenType)
					assertEquals(5, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(2) => [ // date
					assertEquals(RECORD_TYPE, tokenType)
					assertEquals(6, line)
					assertEquals(3, startChar)
					assertEquals(4, length)
				]
			]
		]
	}
	
	@Test
	def testTypedStdFeaturesAreMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			metaType foo string
			
			library function bar(param int) date
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(3, size)
				get(0) => [ // string
					assertEquals(BASIC_TYPE, tokenType)
					assertEquals(2, line)
					assertEquals(13, startChar)
					assertEquals(6, length)
				]
				get(1) => [ // int
					assertEquals(TYPE_ALIAS, tokenType)
					assertEquals(4, line)
					assertEquals(27, startChar)
					assertEquals(3, length)
				]
				get(2) => [ // date
					assertEquals(RECORD_TYPE, tokenType)
					assertEquals(4, line)
					assertEquals(32, startChar)
					assertEquals(4, length)
				]
			]
		]
	}
	
	@Test
	def testDocAnnotationsAreMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			body Authority CFTC
			
			corpus Regulation "CFTC 17 CFR Parts 45" Part45
			
			segment appendix
			segment dataElement
			segment field
			
			reporting rule Cleared
				[regulatoryReference CFTC Part45 appendix "1" dataElement "1" field "Cleared"
					provision "Indicator of whether the transaction has been cleared, or is intended to be cleared, by a central counterparty."]
				return "Y"
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(4, size)
				get(0) => [ // Part45
					assertEquals(DOCUMENT_CORPUS, tokenType)
					assertEquals(11, line)
					assertEquals(27, startChar)
					assertEquals(6, length)
				]
				get(1) => [ // appendix
					assertEquals(DOCUMENT_SEGMENT, tokenType)
					assertEquals(11, line)
					assertEquals(34, startChar)
					assertEquals(8, length)
				]
				get(2) => [ // dataElement
					assertEquals(DOCUMENT_SEGMENT, tokenType)
					assertEquals(11, line)
					assertEquals(47, startChar)
					assertEquals(11, length)
				]
				get(3) => [ // field
					assertEquals(DOCUMENT_SEGMENT, tokenType)
					assertEquals(11, line)
					assertEquals(63, startChar)
					assertEquals(5, length)
				]
			]
		]
	}
	
	@Test
	def testPartialDocAnnotationsDoNotCrashSemanticTokens() {
		testSemanticToken[
			val model = '''
			namespace test
			
			body Authority CFTC
			
			corpus Regulation "CFTC 17 CFR Parts 45" Part45
			
			segment appendix
			segment dataElement
			segment field
			
			type A:
				a int (1..1)
			
			reporting rule Cleared
				[regulatoryReference CFTC Pa
				
			type B:
				a string (1..1)
			'''
			it.model = model
			it.assertNoIssues = false;
			it.assertSemanticTokens = [
				assertEquals(2, size)
				get(0) => [ // int
					assertEquals(TYPE_ALIAS, tokenType)
					assertEquals(11, line)
					assertEquals(3, startChar)
					assertEquals(3, length)
				]
				get(1) => [ // string
					assertEquals(BASIC_TYPE, tokenType)
					assertEquals(17, line)
					assertEquals(3, startChar)
					assertEquals(6, length)
				]
			]
		]
	}
	
	@Test
	def testMetadataAnnotationIsMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			metaType scheme string
			
			type Foo:
				bar string (1..1)
					[metadata scheme]
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(3, size)
				get(2) => [ // scheme
					assertEquals(META_MEMBER, tokenType)
					assertEquals(6, line)
					assertEquals(12, startChar)
					assertEquals(6, length)
				]
			]
		]
	}
	
	@Test
	def testPartialMetadataAnnotationDoesNotCrashSemanticTokens() {
		testSemanticToken[
			val model = '''
			namespace test
			
			metaType scheme string
			
			type Foo:
				bar string (1..1)
					[metadata ]
			'''
			it.model = model
			it.assertNoIssues = false;
			it.assertSemanticTokens = [
				assertEquals(2, size)
			]
		]
	}
	
	@Test
	def testExpressionsAreMarked() {
		testSemanticToken[
			val model = '''
			namespace test
			
			metaType scheme string
			
			enum A:
				V
			
			type Foo:
				bar string (1..1)
					[metadata scheme]
				a A (1..1)
			
			func Bar:
				inputs:
					foo Foo (1..1)
				output:
					result string (0..1)
				set result:
					if foo -> a = A -> V
					then foo -> bar -> scheme
			'''
			it.model = model
			it.assertSemanticTokens = [
				assertEquals(13, size)
				get(4) => [ // Foo
					assertEquals(TYPE, tokenType)
					assertEquals(14, line)
					assertEquals(6, startChar)
					assertEquals(3, length)
				]
				get(5) => [ // string
					assertEquals(BASIC_TYPE, tokenType)
					assertEquals(16, line)
					assertEquals(9, startChar)
					assertEquals(6, length)
				]
				get(6) => [ // foo
					assertEquals(PARAMETER, tokenType)
					assertEquals(18, line)
					assertEquals(5, startChar)
					assertEquals(3, length)
				]
				get(7) => [ // a
					assertEquals(PROPERTY, tokenType)
					assertEquals(18, line)
					assertEquals(12, startChar)
					assertEquals(1, length)
				]
				get(8) => [ // A
					assertEquals(ENUM, tokenType)
					assertEquals(18, line)
					assertEquals(16, startChar)
					assertEquals(1, length)
				]
				get(9) => [ // V
					assertEquals(ENUM_MEMBER, tokenType)
					assertEquals(18, line)
					assertEquals(21, startChar)
					assertEquals(1, length)
				]
				get(10) => [ // foo
					assertEquals(PARAMETER, tokenType)
					assertEquals(19, line)
					assertEquals(7, startChar)
					assertEquals(3, length)
				]
				get(11) => [ // bar
					assertEquals(PROPERTY, tokenType)
					assertEquals(19, line)
					assertEquals(14, startChar)
					assertEquals(3, length)
				]
				get(12) => [ // scheme
					assertEquals(META_MEMBER, tokenType)
					assertEquals(19, line)
					assertEquals(21, startChar)
					assertEquals(6, length)
				]
			]
		]
	}
}