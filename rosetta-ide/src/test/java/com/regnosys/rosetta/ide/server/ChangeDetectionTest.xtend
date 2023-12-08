package com.regnosys.rosetta.ide.server

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import org.eclipse.xtext.testing.TextDocumentConfiguration
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.Position
import java.util.Map
import org.junit.jupiter.api.BeforeEach
import static org.junit.jupiter.api.Assertions.*

class ChangeDetectionTest extends AbstractRosettaLanguageServerTest {
	Map<String, Integer> versionMap
	
	@BeforeEach
	def void beforeEach() {
		versionMap = newHashMap
		
		initializeContext(new TextDocumentConfiguration)
	}
	
	private def String createModel(String fileName, String model) {
		val uri = fileName.writeFile(model)
		open(uri, model)
		versionMap.put(uri, 1)
		return uri
	}
	private def void makeChange(String uri, int line, int character, String oldText, String newText) {
		val newVersion = versionMap.get(uri) + 1
		languageServer.didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(uri, newVersion),
                #[
                	new TextDocumentContentChangeEvent(
                		new Range(new Position(line, character), new Position(line, character + oldText.length)),
                        newText
                    )
                ]
        ));
        versionMap.put(uri, newVersion)
	}
	
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
		assertEquals("Expected type 'int' but was 'string'", issues.head.message)
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
		assertEquals("Cardinality mismatch - cannot assign list to a single value.", issues.head.message)
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
		assertEquals("Expected type 'MyType' but was 'MyType'", issues.head.message)
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
		assertEquals(2, issues.size)
		assertEquals("Expected type 'int' but was 'string'", issues.head.message)
	}
}
