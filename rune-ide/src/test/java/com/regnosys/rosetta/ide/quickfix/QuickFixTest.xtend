package com.regnosys.rosetta.ide.quickfix

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import static org.junit.jupiter.api.Assertions.*
import org.eclipse.lsp4j.Position
import jakarta.inject.Inject
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
			it.assertCodeActions = [
				assertEquals(2, size)
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]
				
				sorted.get(0).getRight => [
					assertEquals("Add `then`", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
				
				sorted.get(1).getRight => [
					assertEquals("Remove square brackets", title)
					assertEquals(edit, null) // make sure no edits are made at this point

				]
			]
		]
	}
	
	@Test
	def testResolveRedundantSquareBrackets() {
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
		testResultCodeAction[
			it.model = model
			it.assertCodeActionResolution = [
				assertEquals(2, size)
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.diagnostics.head.range.start, b.diagnostics.head.range.start)]
				
				sorted.get(0)=> [
					assertEquals("Add `then`", title)
					edit.changes.values.head.head => [
						assertEquals("then extract", newText)
						assertEquals(new Position(12, 3), range.start)
						assertEquals(new Position(12, 10), range.end)
					]
				]
				sorted.get(1)=> [
					assertEquals("Remove square brackets", title)
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
	def testQuickFixDuplicateImport() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.foo.*
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: foo -> a
		'''
		testCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			'''}
			assertCodeActions = [
				assertEquals(1, size) // duplicate import
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]
				
				sorted.get(0).getRight => [
					assertEquals("Optimize imports", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
			]
		]
	}

	//This feature to remove duplicate is removed temperately
	@Test
	def testResolveDuplicateImport() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.* as foo
		import dsl.foo.* as foo
        import dsl.foo.*
        import dsl.foo.*
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: foo -> a
		'''
		
		testResultCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			'''}
			it.assertCodeActionResolution = [
				assertEquals(3, size) //duplicate import
			
				val sorted = it.sortWith[a,b| ru.comparePositions(a.diagnostics.head.range.start, b.diagnostics.head.range.start)]
				
				sorted.get(0) => [
					assertEquals("Optimize imports", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.foo.* as foo\nimport dsl.foo.* as foo\nimport dsl.foo.*\nimport dsl.foo.*", newText) // second import is deleted
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(5, 24), range.end)
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
			
			set result: foo -> a
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
				
				val sorted = it.sortWith[a,b| a.getRight.title.compareTo(b.getRight.title)]
				
				sorted.get(0).getRight => [
					assertEquals("Optimize imports", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
				sorted.get(1).getRight => [
					assertEquals("Sort imports", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]	
			]
		]
	}
	
	@Test
	def testResolveUnusedImport() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.bar.*
		
		func Bar:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result: foo -> a
		'''
		
		testResultCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			'''}
			it.assertCodeActionResolution = [
				assertEquals(2, size) //one unused, one 'Sort Imports' codeAction
				
				val sorted = it.sortWith[a,b| a.title.compareTo(b.title)]
			
				sorted.get(0)=> [
					assertEquals("Optimize imports", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.bar.*\nimport dsl.foo.*", newText) // second import is deleted
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
				sorted.get(1)=> [
					assertEquals("Sort imports", title)
					edit.changes.values.head.head => [
						assertEquals("import dsl.bar.*\nimport dsl.foo.*", newText) // imports are sorted
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
			]
		]
	}
	
	@Test
	def testQuickFixUnsortedImports() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import dsl.aaa.*
		
		func Bar:
			inputs: 
				foo Foo (1..1)
				aaa Aaa (1..1)
			output: result int (1..1)
			
			set result: aaa -> a
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
				assertEquals(1, size) // one 'Sort Imports' codeAction
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]
				
				sorted.get(0).getRight => [
					assertEquals("Sort imports", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
			]
		]
	}

	@Test
	def testResolveUnsortedImports() {
		val model = '''
		namespace foo.bar
		
		import dsl.foo.*
		import asl.aaa.*
		
		func Bar:
			inputs: foo Foo (1..1)
					aaa Aaa (1..1)
			output: result int (1..1)
			
			set result: foo -> a
		'''
		
		testResultCodeAction[
			it.model = model
			it.filesInScope = #{"foo.rosetta" -> '''
				namespace dsl.foo
				
				type Foo:
					a int (1..1)
			''', "ach.rosetta" -> '''
				namespace asl.aaa
								
				type Aaa:
					a int (1..1)
			'''}
			it.assertCodeActionResolution = [
				assertEquals(1, size)
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.diagnostics.head.range.start, b.diagnostics.head.range.start)]
				
				sorted.get(0) => [
					assertEquals("Sort imports", title)
					edit.changes.values.head.head => [
						assertEquals("import asl.aaa.*\n\nimport dsl.foo.*", newText) // imports are sorted
						assertEquals(new Position(2, 0), range.start)
						assertEquals(new Position(3, 16), range.end)
					]
				]
			]
		]
	}
	
	
	@Test
    def testQuickFixConstructorAttributes() {
    	val model = '''
		namespace test
		
		type T:
			a string (1..1)
		func F:
			output: t T (1..1)
			set t : T {}
		'''
		
		testCodeAction[
			it.model = model
			assertCodeActions = [
				assertEquals(2, size) // mandatory attributes + all attributes quickfixes
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]

				sorted.get(0).getRight => [
					assertEquals("Add all attributes", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
				
				sorted.get(1).getRight => [
					assertEquals("Add mandatory attributes", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
			]
		]
    }
    
    
    @Test
	def testResolveConstructorAttributes() {
		val model = '''
		namespace test
		
		type T:
			a string (1..1)
			b string (0..1)
		func F:
			output: t T (1..1)
			set t : T {}
		'''
		
		testResultCodeAction[	
			it.model = model
			it.assertCodeActionResolution = [
				assertEquals(2, size) // mandatory attributes + all attributes quickfixes
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.diagnostics.head.range.start, b.diagnostics.head.range.start)]
				
				sorted.get(0) => [
					assertEquals("Add all attributes", title)
					edit.changes.values.head.head => [
						val expectedResult = '''
						T {
						            a: empty,
						            b: empty
						        }
						'''
						assertEquals(expectedResult, newText) // all attributes are added
						assertEquals(new Position(7, 9), range.start)
						assertEquals(new Position(8, 0), range.end)
					]
				]
				
				sorted.get(1) => [
					assertEquals("Add mandatory attributes", title)
					edit.changes.values.head.head => [
						val expectedResult = '''
						T {
						            a: empty,
						            ...
						        }
						'''
						assertEquals(expectedResult, newText) // mandatory attribute is added
						assertEquals(new Position(7, 9), range.start)
						assertEquals(new Position(8, 0), range.end)
					]
				]
			]
		]
	}
	
	@Test
    def testQuickFixChoiceAttributes() {
    	val model = '''
		namespace test
		
		type A:
			n string (0..1)
		
		type B:
			m string (0..1)
		
		choice C:
			A
			B
		
		func F:
			output: c C (1..1)
			set c : C {}
		'''
		
		testCodeAction[
			it.model = model
			assertCodeActions = [
				assertEquals(2, size) // mandatory attributes + all attributes quickfixes
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.getRight.diagnostics.head.range.start, b.getRight.diagnostics.head.range.start)]

				sorted.get(0).getRight => [
					assertEquals("Add all attributes", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
				
				sorted.get(1).getRight => [
					assertEquals("Add mandatory attributes", title)
					assertEquals(edit, null) // make sure no edits are made at this point
				]
			]
		]
    }
    
    
    @Test
	def testResolveChoiceAttributes() {
		val model = '''
		namespace test
		
		type A:
			n string (0..1)
		
		type B:
			m string (0..1)
		
		choice C:
			A
			B
		
		func F:
			output: c C (1..1)
			set c : C {}
		'''
		
		testResultCodeAction[	
			it.model = model
			it.assertCodeActionResolution = [
				assertEquals(2, size) // mandatory attributes + all attributes quickfixes
				
				val sorted = it.sortWith[a,b| ru.comparePositions(a.diagnostics.head.range.start, b.diagnostics.head.range.start)]
				
				sorted.get(0) => [
					assertEquals("Add all attributes", title)
					edit.changes.values.head.head => [
						val expectedResult = '''
						C {
						            A: empty,
						            B: empty
						        }
						'''
						assertEquals(expectedResult, newText) // all attributes are added
						assertEquals(new Position(14, 9), range.start)
						assertEquals(new Position(15, 0), range.end)
					]
				]
				
				sorted.get(1) => [
					assertEquals("Add mandatory attributes", title)
					edit.changes.values.head.head => [
						val expectedResult = '''
						C {
						            ...
						        }
						'''
						assertEquals(expectedResult, newText) // mandatory attribute is added
						assertEquals(new Position(14, 9), range.start)
						assertEquals(new Position(15, 0), range.end)
					]
				]
			]
		]
	}
}