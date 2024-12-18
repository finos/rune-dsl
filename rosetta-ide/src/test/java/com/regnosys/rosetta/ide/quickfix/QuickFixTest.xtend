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
			it.assertCodeActions = [
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
}