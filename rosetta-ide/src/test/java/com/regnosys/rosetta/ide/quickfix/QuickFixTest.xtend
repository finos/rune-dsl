package com.regnosys.rosetta.ide.quickfix

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import static org.junit.jupiter.api.Assertions.*
import org.eclipse.lsp4j.Position
import javax.inject.Inject
import com.regnosys.rosetta.ide.util.RangeUtils

class QuickFixTest extends AbstractRosettaLanguageServerTest {
	@Inject RangeUtils ru
	
	@Test
	def testQuickFixRedundantSquareBrackets() {
		val model = '''
		namespace foo.bar
		
		type Foo:
			a int (1..1)
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result:
				foo
					extract [ a ]
					extract [ 42 ]
		'''
		testCodeAction[
			it.model = model
			assertCodeActions = [
				assertEquals(2, size)
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]
				
				sorted.get(0).getRight => [
					assertEquals("Add `then`.", title)
					edit.changes.values.head.head => [
						assertEquals("then extract", newText)
						assertEquals(new Position(12, 3), range.start)
						assertEquals(new Position(12, 10), range.end)
					]
				]
				
				sorted.get(1).getRight => [
					assertEquals("Remove square brackets.", title)
					edit.changes.values.head.head => [
						assertEquals("42", newText)
						assertEquals(new Position(12, 11), range.start)
						assertEquals(new Position(12, 17), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testQuickFixDeprecatedMap() {
		val model = '''
		namespace foo.bar
		
		type Foo:
			a int (1..1)
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: foo map a
		'''
		testCodeAction[
			it.model = model
			assertCodeActions = [
				assertEquals(1, size)
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]
				
				sorted.get(0).getRight => [
					assertEquals("Replace with `extract`.", title)
					edit.changes.values.head.head => [
						assertEquals("extract", newText)
						assertEquals(new Position(9, 17), range.start)
						assertEquals(new Position(9, 20), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testQuickFixDuplicateImport() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.foo.*
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: fooImport.returnResult()
		'''
		testCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			'''}
			assertCodeActions = [
				assertEquals(2, size) // one duplicate, one 'Sort Imports' codeAction
				
				it.get(0).getRight => [
					assertEquals("Optimize imports.", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.foo.*", newText)
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testQuickFixUnusedImport() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.bar.*
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: fooImport.returnResult()
		'''
		testCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			'''}
			assertCodeActions = [
				assertEquals(2, size) //one unused, one 'Sort Imports' codeAction
				
				it.get(0).getRight => [
					assertEquals("Optimize imports.", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.foo.*", newText)
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testQuickFixSortImports() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.bar.*
		import dsl.foo.*
		import dsl.aaa.*
		
		func Bar:
			inputs: 
				foo Foo (1..1)
				aaa Aaa (1..1)
			output: result int (1..1)
			
			set result: aaa.a
		'''
		testCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			''', "ach.rosetta" -> '''
				namespace dsl.aaa
								
				type Aaa:
					a int (1..1)
			'''}
			assertCodeActions = [
				assertEquals(3, size) //one unused, one duplicate, one 'Sort Imports' codeAction
				
				it.get(0).getRight => [
					assertEquals("Optimize imports.", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.aaa.*\nimport dsl.foo.*", newText)
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(5, 16), range.end)
					]
				]
				
				it.get(2).getRight => [
					assertEquals("Sort Imports.", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.aaa.*\nimport dsl.foo.*", newText)
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(5, 16), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testSortingImports() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.aaa.*
		
		func Bar:
			inputs: 
				foo Foo (1..1)
				aaa Aaa (1..1)
			output: result int (1..1)
			
			set result: aaa.a
		'''
		testCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			''', "ach.rosetta" -> '''
				namespace dsl.aaa
								
				type Aaa:
					a int (1..1)
			'''}
			assertCodeActions = [
				assertEquals(1, size) //one 'Sort Imports' codeAction
				
				it.get(0).getRight => [
					assertEquals("Sort Imports.", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.aaa.*\nimport dsl.foo.*", newText)
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
			]
		]
	}
	
}