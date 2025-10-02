package com.regnosys.rosetta.ide.issues

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest
import org.junit.jupiter.api.Test


// Issue https://github.com/finos/rune-dsl/issues/785
class Issue785 extends AbstractRosettaLanguageServerValidationTest {
	@Test
	def void testIssue785() {
		val reportURI = createModel("foo-report.rosetta", '''
			namespace foo
			
			import bar.*
			
			body Authority Body
			
			corpus Regulations Corpus 
			
			report Body Corpus in T+2
			    from Event
			    when Eligible
			    with type Report
			
			eligibility rule Eligible from Event:
			    item
			
			reporting rule Thing from Event:
			    ""
		''')
		createModel("foo-type.rosetta", '''
			namespace foo
			
			type Report:
			    thing string (1..1)
			        [ruleReference Thing]
		''')
		val barURI = createModel("bar-type.rosetta", '''
			namespace bar
			
			type Event:
		''')
		
		// Initial: there should be no issues.
		assertNoIssues
		
		// Introduce reference errors in `foo-report.rosetta` by changing the name of `bar.Event` to `bar.Even`.
		makeChange(barURI, 2, 5, "Event", "Even")
		
		val issues = diagnostics.get(reportURI)
		assertIssues(
			'''
			Error [[9, 9] .. [9, 14]]: Couldn't resolve reference to RosettaType 'Event'.
			Error [[13, 31] .. [13, 36]]: Couldn't resolve reference to RosettaType 'Event'.
			Error [[16, 26] .. [16, 31]]: Couldn't resolve reference to RosettaType 'Event'.
			Warning [[2, 7] .. [2, 12]]: Unused import bar.*
			''',
			issues
		)
		
		// Change back
		makeChange(barURI, 2, 5, "Even", "Event")
		
		// All issues should disappear.
		assertNoIssues
	}
}