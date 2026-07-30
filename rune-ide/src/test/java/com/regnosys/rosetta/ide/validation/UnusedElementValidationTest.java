package com.regnosys.rosetta.ide.validation;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

/**
 * The "is never used" editor marker, across every kind of declaration that can carry one: functions,
 * types (including choice types), enumerations and rules.
 *
 * <p>Detection is a single generic walk over cross-references rather than a check per grammar rule, so the
 * point of the many cases below is coverage of the <em>reference shapes</em> — each one is a way a
 * declaration can be used that a narrower implementation would miss, and therefore a distinct regression
 * risk.
 *
 * <p>Where a test needs scaffolding that would itself attract a marker, that scaffolding is annotated
 * ({@code [rootType]} on a type, {@code [suppressUnused]} on a function) so the expected marker set stays
 * exactly the one behaviour under test. Rules cannot be annotated — {@code RosettaRule} is not
 * {@code Annotated} in the grammar — so they have no opt-out.
 */
public class UnusedElementValidationTest extends AbstractRosettaLanguageServerValidationTest {

	// ---------------------------------------------------------------- functions

	@Test
	void unusedFunctionIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Unused:
					output: result int (1..1)
					set result: 42
				""");

		// A Hint with the Unnecessary tag is not a "problem" (severity > Warning), so the model is valid.
		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Function 'Unused' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary),
				"Expected the diagnostic to carry the Unnecessary tag so the editor greys it out");
	}

	@Test
	void calledFunctionIsNotMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Used:
					output: result int (1..1)
					set result: 42

				func Caller:
					output: result int (1..1)
					set result: Used()
				""");

		assertNoIssues();

		// Only `Caller` is unused; `Used` is called by `Caller`.
		Assertions.assertEquals(List.of("Function 'Caller' is never used"), unusedMarkers(uri));
	}

	@Test
	void suppressUnusedOptsOut() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Unused:
					[suppressUnused]
					output: result int (1..1)
					set result: 42
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * A function whose only caller is itself has no entry point, so it is dead code. This is a deliberate
	 * change from the pre-generalisation behaviour, where a self-recursive function counted as used: the
	 * generic walk attributes each reference to the declaration containing it and drops the ones that point
	 * back at their own declaration.
	 */
	@Test
	void selfRecursiveFunctionWithNoOtherCallerIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				func Countdown:
					inputs: n int (1..1)
					output: result int (1..1)
					set result:
						if n <= 0 then 0 else Countdown(n - 1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Function 'Countdown' is never used"), unusedMarkers(uri));
	}

	/**
	 * [ingest XML] functions are called from outside the model (by the runtime), so they must not be
	 * flagged as unused. This test also verifies that `XML` resolves without a linking error — if
	 * basictypes.rosetta is not loaded properly, assertNoIssues() would fail with a Linking diagnostic.
	 */
	@Test
	void ingestAnnotatedFunctionIsNotMarkedAsUnused() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Foo:
					[rootType]
					a string (1..1)

				func IngestFoo:
					[ingest XML]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void enrichAnnotatedFunctionIsNotMarkedAsUnused() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Foo:
					[rootType]
					a string (1..1)

				func EnrichFoo:
					[enrich]
					inputs:
						input Foo (1..1)
					output:
						result Foo (1..1)
					set result: input
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void projectionAnnotatedFunctionIsNotMarkedAsUnused() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Foo:
					[rootType]
					a string (1..1)

				func ProjectFoo:
					[projection XML]
					inputs:
						input Foo (1..1)
					output:
						result string (1..1)
					set result: input -> a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// -------------------------------------------------------------------- types

	@Test
	void unusedTypeIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Unused:
					a string (1..1)
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Type 'Unused' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/** {@code TypeCall.type} — the most common shape: an attribute's type. */
	@Test
	void typeUsedAsAttributeTypeIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Used:
					a string (1..1)

				type Holder:
					[rootType]
					u Used (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code TypeCall.type} — function input and output types. */
	@Test
	void typeUsedAsFunctionInputOrOutputIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type UsedInput:
					a string (1..1)

				type UsedOutput:
					b string (1..1)

				func Convert:
					[suppressUnused]
					inputs: source UsedInput (1..1)
					output: result UsedOutput (1..1)
					set result: UsedOutput { b: source -> a }
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code Data.superType} — {@code extends}. */
	@Test
	void typeUsedAsSuperTypeIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Base:
					a string (1..1)

				type Sub extends Base:
					[rootType]
					b string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code AsOperation.type} — {@code as Foo}. */
	@Test
	void typeUsedByAsOperationIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Base:
					[rootType]
					a string (1..1)

				type Sub extends Base:
					b string (1..1)

				func Narrow:
					[suppressUnused]
					inputs: source Base (1..1)
					output: result Sub (0..1)
					set result: source as Sub
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code SwitchCaseGuard.referenceGuard} — a {@code switch} case naming a type. */
	@Test
	void typeUsedBySwitchCaseGuardIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Base:
					[rootType]
					a string (1..1)

				type Sub extends Base:
					b string (1..1)

				func Describe:
					[suppressUnused]
					inputs: source Base (1..1)
					output: result string (0..1)
					set result:
						source switch
							Sub then "sub",
							default empty
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code RosettaQualifiableConfiguration.rosettaClass} — {@code isEvent root}. */
	@Test
	void typeUsedAsQualifiableRootIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				isEvent root Root;

				type Root:
					a string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * {@code RosettaDataReference.data} — a type named at the head of an annotation path. The path points at
	 * an attribute, so this also exercises container rollup.
	 */
	@Test
	void typeUsedInAnnotationPathIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Target:
					value string (1..1)
						[metadata location]

				type Pointer:
					[rootType]
					ref string (1..1)
						[metadata address "pointsTo"=Target->value]
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void rootTypeAnnotationOptsOut() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Entry:
					[rootType]
					a string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void suppressUnusedOptsOutForType() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Published:
					[suppressUnused]
					a string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * Self-reference exclusion: a type that only refers to itself has no external user, so the recursion
	 * must not make it look used.
	 */
	@Test
	void recursiveTypeWithNoOtherUserIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Node:
					value string (1..1)
					child Node (0..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Type 'Node' is never used"), unusedMarkers(uri));
	}

	/** {@code Choice extends Data}, so choice types are candidates too. */
	@Test
	void unusedChoiceTypeIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Option:
					[rootType]
					a string (1..1)

				choice UnusedChoice:
					Option
					string
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Type 'UnusedChoice' is never used"), unusedMarkers(uri));
	}

	/** A choice option is a {@code TypeCall}, so listing a type as an option counts as using it. */
	@Test
	void typeUsedAsChoiceOptionIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Option:
					a string (1..1)

				choice Either:
					[rootType]
					Option
					string
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// ------------------------------------------------------------- enumerations

	@Test
	void unusedEnumerationIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Unused:
					VALUE1
					VALUE2
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Enumeration 'Unused' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/** {@code TypeCall.type} — an enum used as an attribute's type. */
	@Test
	void enumerationUsedAsAttributeTypeIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Colour:
					RED
					GREEN

				type Painted:
					[rootType]
					colour Colour (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code RosettaEnumeration.parent} — {@code extends}. */
	@Test
	void enumerationUsedAsParentIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Base:
					A

				enum Extended extends Base:
					[suppressUnused]
					B
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code RosettaEnumValueReference.enumeration} — {@code Colour -> RED}. */
	@Test
	void enumerationUsedByEnumValueReferenceIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Colour:
					RED
					GREEN

				func Favourite:
					[suppressUnused]
					output: result Colour (1..1)
					set result: Colour -> RED
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code ToEnumOperation.enumeration} — {@code to-enum}. */
	@Test
	void enumerationUsedByToEnumOperationIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Colour:
					RED
					GREEN

				func Parse:
					[suppressUnused]
					inputs: name string (1..1)
					output: result Colour (0..1)
					set result: name to-enum Colour
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * Container rollup: a {@code switch} case guard on an enum names an enum <em>value</em>, never the
	 * enumeration itself. Without attributing each reference to the declaration containing its target, an
	 * enum used only this way would be a false positive — which is also what would happen to the built-in
	 * {@code SerializationFormat} enum, referenced only through its values by {@code schema} declarations.
	 */
	@Test
	void enumerationUsedOnlyViaOneOfItsValuesIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Colour:
					RED
					GREEN

				type Palette:
					[rootType]
					preferred Colour (1..1)

				func Preferred:
					[suppressUnused]
					inputs: palette Palette (1..1)
					output: result string (0..1)
					set result:
						palette -> preferred switch
							RED then "red",
							default empty
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void suppressUnusedOptsOutForEnumeration() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum Published:
					[suppressUnused]
					A
					B
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// -------------------------------------------------------------------- rules

	/** {@code RosettaReport.eligibilityRules} — the only shape that uses an eligibility rule. */
	@Test
	void eligibilityRuleUsedByReportIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				body Authority Auth
				corpus Auth Doc

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)

				eligibility rule Eligible from Input:
					True

				report Auth Doc in T+1
					from Input when Eligible
					with type Output
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	@Test
	void eligibilityRuleWithNoReportIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				eligibility rule Eligible from Input:
					True
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Eligibility rule 'Eligible' is not used by any report", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	@Test
	void unusedReportingRuleIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				reporting rule Unused from Input:
					extract a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Reporting rule 'Unused' is never used"), unusedMarkers(uri));
	}

	/** {@code RuleReferenceAnnotation.reportingRule} on an attribute, which rolls up to the enclosing type. */
	@Test
	void reportingRuleUsedByAttributeRuleReferenceIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)
						[ruleReference Extract]

				reporting rule Extract from Input:
					extract a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code RuleReferenceAnnotation.reportingRule} inside a {@code rule source}. */
	@Test
	void reportingRuleUsedByRuleSourceIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)

				rule source Overrides
				{
					Output:
						+ a
							[ruleReference Extract]
				}

				reporting rule Extract from Input:
					extract a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * {@code RosettaSymbolReference.symbol} — a rule is a {@code RosettaCallableWithArgs}, so it can be
	 * invoked from another rule's expression exactly like a function.
	 */
	@Test
	void reportingRuleUsedByAnotherRuleIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)
						[ruleReference Outer]

				reporting rule Outer from Input:
					Inner

				reporting rule Inner from Input:
					extract a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * A {@code - attr} removal in a rule source names the attribute, never a rule, so it creates no
	 * reference to a rule: {@code Unmentioned} stays flagged even though a rule source is present. It also
	 * cannot un-use a rule — the {@code [ruleReference Used]} it removes is still a reference, so
	 * {@code Used} counts as used. That is the consequence of keeping detection purely syntactic: the
	 * effective-rule computation that resolves {@code +}/{@code -} overrides works on the {@code RAttribute}
	 * layer and is deliberately invisible here.
	 */
	@Test
	void ruleSourceRemovalCreatesNoRuleReference() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)
						[ruleReference Used]

				rule source Overrides
				{
					Output:
						- a
				}

				reporting rule Used from Input:
					extract a

				reporting rule Unmentioned from Input:
					extract a
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Reporting rule 'Unmentioned' is never used"), unusedMarkers(uri));
	}

	// ----------------------------------------------------------- interoperation

	/**
	 * The markers are added to the issues the standard validation produced, so a model with real problems
	 * must still report them. Regression test for {@code RosettaLanguageServerImpl#toDiagnostic}: issues
	 * with no code exist, and probing an immutable {@code Set} with {@code null} throws — which used to
	 * discard every diagnostic for the resource, turning an invalid model into a silently clean one.
	 */
	@Test
	void validationErrorsWithNoIssueCodeAreStillReported() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Output:
					[rootType]
					a string (1..1)

				rule source Overrides
				{
					Output:
						- a
				}
				""");

		List<String> messages = getDiagnostics().get(uri).stream().map(Diagnostic::getMessage).toList();
		Assertions.assertTrue(messages.contains("There is no rule reference to remove"),
				"Expected the validation error to survive alongside the unused markers, but got " + messages);
	}

	/**
	 * Messages of the diagnostics that render as a faded "unused" marker, i.e. those carrying the
	 * {@link DiagnosticTag#Unnecessary} tag, sorted so the assertions do not depend on declaration order.
	 * Returns an empty list when the file has no diagnostics at all, so that "no marker" and "no
	 * diagnostics" compare equal.
	 */
	private List<String> unusedMarkers(String uri) {
		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		if (diagnostics == null) {
			return List.of();
		}
		return diagnostics.stream()
				.filter(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary))
				.map(Diagnostic::getMessage)
				.sorted()
				.toList();
	}
}
