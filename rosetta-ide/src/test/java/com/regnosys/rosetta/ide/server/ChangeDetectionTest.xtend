package com.regnosys.rosetta.ide.server

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest

class ChangeDetectionTest extends AbstractRosettaLanguageServerValidationTest {	
	@Test
	def void testChangeInAttributeTypeIsPropagated() {
		val typesURI = createModel("types.rosetta", '''
			namespace test
			
			type A:
				attr int (1..1)
		''')
		val funcsURI = createModel("funcs.rosetta", '''
			namespace test
			
			func Foo:
				inputs: input A (1..1)
				output: result int (1..1)
				
				set result: input -> attr
		''')
		
		// Initial: there should be no issue.
		assertNoIssues
		
		// Introduce a type error by changing the type of `attr` from `int` to `string`.
		makeChange(typesURI, 3, 6, "int", "string")
		
		// There should be a type error in func `Foo`
		val issues = diagnostics.get(funcsURI)
		assertEquals(1, issues.size)
		assertEquals("Expected type `int`, but got `string` instead. Cannot assign `string` to output `result`", issues.head.message)
	}
	
	@Test
	def void testChangeInAttributeCardinalityIsPropagated() {
		val typesURI = createModel("types.rosetta", '''
			namespace test
			
			type A:
				attr int (1..1)
		''')
		val funcsURI = createModel("funcs.rosetta", '''
			namespace test
			
			func Foo:
				inputs: input A (1..1)
				output: result int (1..1)
				
				set result: input -> attr
		''')
		
		// Initial: there should be no issue.
		assertNoIssues
		
		// Introduce an error by changing the cardinality of `attr` from `(1..1)` to `(0..*)`.
		makeChange(typesURI, 3, 10, "(1..1)", "(0..*)")
		
		// There should be a cardinality error in func `Foo`
		val issues = diagnostics.get(funcsURI)
		assertEquals(1, issues.size)
		assertEquals("Expecting single cardinality. Cannot assign a list to a single value", issues.head.message)
	}
	
	@Test
	def void testChangeInAttributeQualifiedTypeIsPropagated() {
		createModel("foo.rosetta", '''
			namespace foo
			
			type MyType:
		''')
		createModel("bar.rosetta", '''
			namespace bar
			
			type MyType:
		''')
		val typesURI = createModel("types.rosetta", '''
			namespace test
			
			import foo.MyType
			
			type A:
				attr MyType (1..1)
		''')
		val funcsURI = createModel("funcs.rosetta", '''
			namespace test
			
			import foo.MyType
			
			func Foo:
				inputs: input A (1..1)
				output: result MyType (1..1)
				
				set result: input -> attr
		''')
		
		// Initial: there should be no issue.
		assertNoIssues
		
		// Introduce a type error by changing the type of `attr` from `foo.MyType` to `bar.MyType`.
		// We do this by changing `import foo.MyType` to `import bar.MyType`.
		makeChange(typesURI, 2, 7, "foo", "bar")
		
		// There should be a type error in func `Foo`
		val issues = diagnostics.get(funcsURI)
		assertEquals(1, issues.size)
		assertEquals("Expected type `foo.MyType`, but got `bar.MyType` instead. Cannot assign `bar.MyType` to output `result`", issues.head.message)
	}
	
	@Test
	def void testChangeInRuleInputTypeIsPropagated() {
		val ruleAURI = createModel("ruleA.rosetta", '''
			namespace test
			
			reporting rule A from string:
				42
		''')
		val ruleBURI = createModel("ruleB.rosetta", '''
			namespace test
			
			reporting rule B from string:
				A
		''')
		
		// Initial: there should be no issue.
		assertNoIssues
		
		// Introduce a type error by changing the input type of rule `A` to `int`.
		makeChange(ruleAURI, 2, 22, "string", "int")
		
		// There should be a type error in rule B
		val issues = diagnostics.get(ruleBURI)
		assertEquals(1, issues.size)
		assertEquals("Expected type `int`, but got `string` instead. Rule `A` cannot be called with type `string`", issues.head.message)
	}
	
	@Test
	def void testChangeInRuleExpressionIsPropagated() {
		val ruleAURI = createModel("ruleA.rosetta", '''
			namespace test
			
			reporting rule A from string:
				42
		''')
		val funcURI = createModel("func.rosetta", '''
			namespace test
			
			func Foo:
				output:
					result int (1..1)
				set result:
					A("")
		''')
		
		// Initial: there should be no issue.
		assertNoIssues
		
		// Introduce a type error by changing the output of rule `A` to be of type `string`.
		makeChange(ruleAURI, 3, 1, "42", "\"My string\"")
		
		// There should be a type error in func Foo
		val issues = diagnostics.get(funcURI)
		assertEquals(1, issues.size)
		assertEquals("Expected type `int`, but got `string` instead. Cannot assign `string` to output `result`", issues.head.message)
	}
}
