package com.regnosys.rosetta.ide.server;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

import java.util.List;

public class ChangeDetectionTest extends AbstractRosettaLanguageServerValidationTest {
	@Test
	void testChangeInAttributeTypeIsPropagated() {
		String typesURI = createModel("types.rosetta", """
				namespace test

				type A:
					attr int (1..1)
				""");
		String funcsURI = createModel("funcs.rosetta", """
				namespace test

				func Foo:
					inputs: input A (1..1)
					output: result int (1..1)

					set result: input -> attr
				""");

		// Initial: there should be no issue.
		assertNoIssues();

		// Introduce a type error by changing the type of `attr` from `int` to `string`.
		makeChange(typesURI, 3, 6, "int", "string");

		// There should be a type error in func `Foo`
		List<Diagnostic> issues = getDiagnostics().get(funcsURI);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals(
				"Expected type `int`, but got `string` instead. Cannot assign `string` to output `result`",
				issues.get(0).getMessage());
	}

	@Test
	void testChangeInAttributeCardinalityIsPropagated() {
		String typesURI = createModel("types.rosetta", """
				namespace test

				type A:
					attr int (1..1)
				""");
		String funcsURI = createModel("funcs.rosetta", """
				namespace test

				func Foo:
					inputs: input A (1..1)
					output: result int (1..1)

					set result: input -> attr
				""");

		// Initial: there should be no issue.
		assertNoIssues();

		// Introduce an error by changing the cardinality of `attr` from `(1..1)` to
		// `(0..*)`.
		makeChange(typesURI, 3, 10, "(1..1)", "(0..*)");

		// There should be a cardinality error in func `Foo`
		List<Diagnostic> issues = getDiagnostics().get(funcsURI);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals("Expecting single cardinality. Cannot assign a list to a single value",
				issues.get(0).getMessage());
	}

	@Test
	void testChangeInAttributeQualifiedTypeIsPropagated() {
		createModel("foo.rosetta", """
				namespace foo

				type MyType:
				""");
		createModel("bar.rosetta", """
				namespace bar

				type MyType:
				""");
		String typesURI = createModel("types.rosetta", """
				namespace test

				import foo.MyType

				type A:
					attr MyType (1..1)
				""");
		String funcsURI = createModel("funcs.rosetta", """
				namespace test

				import foo.MyType

				func Foo:
					inputs: input A (1..1)
					output: result MyType (1..1)

					set result: input -> attr
				""");

		// Initial: there should be no issue.
		assertNoIssues();

		// Introduce a type error by changing the type of `attr` from `foo.MyType` to
		// `bar.MyType`.
		// We do this by changing `import foo.MyType` to `import bar.MyType`.
		makeChange(typesURI, 2, 7, "foo", "bar");

		// There should be a type error in func `Foo`
		List<Diagnostic> issues = getDiagnostics().get(funcsURI);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals(
				"Expected type `foo.MyType`, but got `bar.MyType` instead. Cannot assign `bar.MyType` to output `result`",
				issues.get(0).getMessage());
	}

	@Test
	void testChangeInRuleInputTypeIsPropagated() {
		String ruleAURI = createModel("ruleA.rosetta", """
				namespace test

				reporting rule A from string:
					42
				""");
		String ruleBURI = createModel("ruleB.rosetta", """
				namespace test

				reporting rule B from string:
					A
				""");

		// Initial: there should be no issue.
		assertNoIssues();

		// Introduce a type error by changing the input type of rule `A` to `int`.
		makeChange(ruleAURI, 2, 22, "string", "int");

		// There should be a type error in rule B
		List<Diagnostic> issues = getDiagnostics().get(ruleBURI);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals(
				"Expected type `int`, but got `string` instead. Rule `A` cannot be called with type `string`",
				issues.get(0).getMessage());
	}

	@Test
	void testChangeInRuleExpressionIsPropagated() {
		String ruleAURI = createModel("ruleA.rosetta", """
				namespace test

				reporting rule A from string:
					42
				""");
		String funcURI = createModel("func.rosetta", """
				namespace test

				func Foo:
					output:
						result int (1..1)
					set result:
						A("")
				""");

		// Initial: there should be no issue.
		assertNoIssues();

		// Introduce a type error by changing the output of rule `A` to be of type
		// `string`.
		makeChange(ruleAURI, 3, 1, "42", "\"My string\"");

		// There should be a type error in func Foo
		List<Diagnostic> issues = getDiagnostics().get(funcURI);
		Assertions.assertEquals(1, issues.size());
		Assertions.assertEquals(
				"Expected type `int`, but got `string` instead. Cannot assign `string` to output `result`",
				issues.get(0).getMessage());
	}
	
	@Test
	void testChangeInLabelShouldRegenerateLabelProviderForReport() {
		createModel("ruleA.rosetta", """
				namespace test

				body Authority Body
				corpus Directive "My corpus" Corpus
				
				report Body Corpus in T+1
					from string
					when FilterEligible
					with type MyReport 
				
				eligibility rule FilterEligible from string:
					item
				""");
		String typeURI = createModel("type.rosetta", """
				namespace test

				type MyReport:
					attr string (1..1)
						[label as "My label"]
				""");
		
		String labelProviderPath = "test/labels/BodyCorpusLabelProvider.java";

		// There should be no issue.
		assertNoIssues();
		// There should be a generated label provider.
		String originalLabelProviderCode = readGeneratedFile(labelProviderPath);
		Assertions.assertNotNull(originalLabelProviderCode, "Label provider does not exist at " + labelProviderPath);

		// Change label to "My new label".
		makeChange(typeURI, 4, 12, "\"My label\"", "\"My new label\"");
		
		// There should again be no issue.
		assertNoIssues();
		// The new label provider should be different.
		String newLabelProviderCode = readGeneratedFile(labelProviderPath);
		Assertions.assertNotNull(newLabelProviderCode, "Label provider does not exist at " + labelProviderPath);
		Assertions.assertNotEquals(originalLabelProviderCode, newLabelProviderCode);
	}
}
