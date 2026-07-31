package com.regnosys.rosetta.ide.validation;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

/**
 * The "is never used" editor marker. Every <em>named</em> root element can carry one, so the cases below
 * range from functions and types through rules, type aliases, annotations and schemas to the documentation
 * elements ({@code body}, {@code corpus}, {@code segment}) and {@code rule source}.
 *
 * <p>Detection is a single generic walk over cross-references rather than a check per grammar rule, so the
 * point of the many cases below is coverage of the <em>reference shapes</em> — each one is a way a
 * declaration can be used that a narrower implementation would miss, and therefore a distinct regression
 * risk.
 *
 * <p>Where a test needs scaffolding that would itself attract a marker, that scaffolding is annotated
 * ({@code [rootType]} on a type, {@code [suppressUnused]} on a function) so the expected marker set stays
 * exactly the one behaviour under test. Only functions, types, enumerations, type aliases and schemas can be
 * annotated; the other kinds have no {@code Annotations} fragment in their grammar rule, so where such a
 * declaration is only scaffolding its marker is asserted rather than suppressed, with a comment saying so.
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
	 * A dispatch case is not referenceable at all: {@code RosettaScopeProvider} keeps {@code FunctionDispatch}
	 * out of the scope a call resolves against, so a caller always points at the main declaration and never at
	 * a case. Flagging one would therefore be a permanent false positive on live, generated-into code — the
	 * same correctness argument that excludes {@code metaType}, not a matter of taste.
	 *
	 * <p>The models below cannot use {@code assertNoIssues()}: the main declaration of a dispatch function has
	 * no body by design, which {@code FunctionValidator} warns about independently of anything here.
	 */
	@Test
	void dispatchCasesOfACalledFunctionAreNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum DayCountFractionEnum:
					ACT_360
					ACT_365L

				func Caller:
					[suppressUnused]
					inputs: dcf DayCountFractionEnum (1..1)
					output: result int (1..1)
					set result: DayCountBasis(dcf)

				func DayCountBasis:
					inputs: dcf DayCountFractionEnum (1..1)
					output: basis int (1..1)

				func DayCountBasis(dcf: DayCountFractionEnum -> ACT_360):
					set basis: 360

				func DayCountBasis(dcf: DayCountFractionEnum -> ACT_365L):
					set basis: 365
				""");

		assertNoErrors(uri);
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * And they stay unflagged when nothing calls the function either, since a case is used exactly when its
	 * main declaration is. Nothing at all is reported here: the main declaration is separately exempt as a
	 * function with no body. {@code Dead} is present so the assertion is not vacuous — it proves markers are
	 * being produced for this model.
	 */
	@Test
	void dispatchCasesOfAnUncalledFunctionAreNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				enum DayCountFractionEnum:
					ACT_360
					ACT_365L

				func DayCountBasis:
					inputs: dcf DayCountFractionEnum (1..1)
					output: basis int (1..1)

				func DayCountBasis(dcf: DayCountFractionEnum -> ACT_360):
					set basis: 360

				func DayCountBasis(dcf: DayCountFractionEnum -> ACT_365L):
					set basis: 365

				func Dead:
					output: result int (1..1)
					set result: 42
				""");

		assertNoErrors(uri);
		Assertions.assertEquals(List.of("Function 'Dead' is never used"), unusedMarkers(uri));
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
		// `Overrides` itself is scaffolding no report references, and a rule source has no opt-out, so its
		// own marker is expected here. What this test is about is that `Extract` does not get one.
		Assertions.assertEquals(List.of("Rule source 'Overrides' is never used"), unusedMarkers(uri));
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
		// `Overrides` is scaffolding no report references, and has no opt-out — see the note on
		// reportingRuleUsedByRuleSourceIsNotFlagged.
		Assertions.assertEquals(
				List.of("Reporting rule 'Unmentioned' is never used", "Rule source 'Overrides' is never used"),
				unusedMarkers(uri));
	}

	// ------------------------------------------------------------- type aliases

	@Test
	void unusedTypeAliasIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				typeAlias Amount: number
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Type alias 'Amount' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/** A type alias is a {@code RosettaType}, so it is used through {@code TypeCall.type} like any type. */
	@Test
	void typeAliasUsedAsAttributeTypeIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				typeAlias Amount: number

				type Trade:
					[rootType]
					notional Amount (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** A type alias's grammar rule now has {@code Annotations*}, so it is one of the suppressible kinds. */
	@Test
	void suppressUnusedOptsOutForTypeAlias() {
		String uri = createModel("model.rosetta", """
				namespace test

				typeAlias Amount: number
					[suppressUnused]
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// -------------------------------------------------------------- annotations

	@Test
	void unusedAnnotationIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				annotation tagged: <"A test annotation.">
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Annotation 'tagged' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/** {@code AnnotationRef.annotation}. */
	@Test
	void annotationAppliedToATypeIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				annotation tagged: <"A test annotation.">

				type Marked:
					[rootType]
					[tagged]
					a string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * {@code AnnotationRef.attribute} — {@code [tagged key]}, the shape the builtin {@code [metadata key]}
	 * uses. The grammar always names the annotation before its attribute, so this cannot be reduced to a pure
	 * container-rollup case; it does confirm that naming an attribute does not somehow <em>stop</em> the
	 * annotation counting as used.
	 */
	@Test
	void annotationUsedWithOneOfItsAttributesIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				annotation tagged: <"A test annotation.">
					key string (0..1)

				type Marked:
					[rootType]
					[tagged key]
					a string (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * An annotation has no {@code Annotations} fragment in its grammar rule, so {@code [suppressUnused]}
	 * cannot be written on one even though {@code Annotation} extends {@code RootElement} and therefore
	 * <em>is</em> {@code Annotated} in the EMF model. Recorded as a test so the gap is visible rather than
	 * discovered.
	 */
	@Test
	void suppressUnusedCannotBeWrittenOnAnAnnotation() {
		createModel("model.rosetta", """
				namespace test

				annotation tagged:
					[suppressUnused]
				""");

		List<String> messages = getDiagnostics().values().stream()
				.flatMap(List::stream)
				.map(Diagnostic::getMessage)
				.toList();
		Assertions.assertTrue(messages.stream().anyMatch(m -> m.contains("suppressUnused")),
				"Expected [suppressUnused] on an annotation to be a syntax error, but got " + messages);
	}

	// ----------------------------------------------------------------- schemas

	@Test
	void unusedSchemaIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				schema fixml XML
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Schema 'fixml' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/** {@code TransformAnnotation.ref} — {@code [ingest fixml]}. */
	@Test
	void schemaUsedByTransformAnnotationIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				schema fixml XML

				type Foo:
					[rootType]
					a string (1..1)

				func IngestFoo:
					[ingest fixml]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** A schema's grammar rule does have {@code Annotations*}, so it is one of the suppressible kinds. */
	@Test
	void suppressUnusedOptsOutForSchema() {
		String uri = createModel("model.rosetta", """
				namespace test

				schema fixml XML
					[suppressUnused]
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// ------------------------------------------------------------ rule sources

	@Test
	void unusedRuleSourceIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)

				type Output:
					[rootType]
					a string (1..1)

				reporting rule Extract from Input:
					extract a

				rule source Overrides
				{
					Output:
						+ a
							[ruleReference Extract]
				}
				""");

		assertNoIssues();

		Assertions.assertEquals(List.of("Rule source 'Overrides' is never used"), unusedMarkers(uri));
		Assertions.assertTrue(getDiagnostics().get(uri).stream()
						.allMatch(d -> d.getSeverity() == DiagnosticSeverity.Hint),
				"A rule source marker must be a Hint like every other unused marker");
	}

	/** {@code RosettaReport.ruleSource} — {@code with source}. */
	@Test
	void ruleSourceUsedByReportIsNotFlagged() {
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

				reporting rule Extract from Input:
					extract a

				rule source Overrides
				{
					Output:
						+ a
							[ruleReference Extract]
				}

				report Auth Doc in T+1
					from Input when Eligible
					with type Output
					with source Overrides
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/** {@code RosettaExternalRuleSource.superSource} — {@code rule source Derived extends Base}. */
	@Test
	void ruleSourceUsedAsSuperSourceIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				type Input:
					[rootType]
					a string (1..1)
					b string (1..1)

				type Output:
					[rootType]
					a string (1..1)
					b string (1..1)

				reporting rule ExtractA from Input:
					extract a

				reporting rule ExtractB from Input:
					extract b

				rule source Base
				{
					Output:
						+ a
							[ruleReference ExtractA]
				}

				rule source Derived extends Base
				{
					Output:
						+ b
							[ruleReference ExtractB]
				}
				""");

		assertNoIssues();
		// `Base` is used by `Derived`'s `extends`. `Derived` itself is used by nothing — non-transitively, so
		// an unused rule source does not stop the source it extends counting as used.
		Assertions.assertEquals(List.of("Rule source 'Derived' is never used"), unusedMarkers(uri));
	}

	// -------------------------------------------------- documentation elements

	@Test
	void unusedBodyCorpusAndSegmentAreMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				body Authority Auth
				corpus Regulation Doc
				segment field
				""");

		assertNoIssues();
		Assertions.assertEquals(
				List.of("Body 'Auth' is never used", "Corpus 'Doc' is never used", "Segment 'field' is never used"),
				unusedMarkers(uri));
	}

	/**
	 * {@code RegulatoryDocumentReference.body} / {@code .corpusList} and {@code RosettaSegmentRef.segment} —
	 * all three reached through a {@code [docReference]}, which hangs off the {@code References} fragment on
	 * an attribute and therefore rolls up to the enclosing type.
	 */
	@Test
	void bodyCorpusAndSegmentUsedByDocReferenceAreNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				body Authority Auth
				corpus Regulation Doc
				segment field

				type Documented:
					[rootType]
					a string (1..1)
						[docReference Auth Doc field "1.2" provision "As described."]
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * {@code RosettaCorpus.body} — a corpus naming its body. Also the clearest illustration of
	 * non-transitivity: {@code Auth} counts as used even though the only thing using it is itself unused.
	 */
	@Test
	void bodyUsedByCorpusIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				body Authority Auth
				corpus Regulation Auth Doc
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of("Corpus 'Doc' is never used"), unusedMarkers(uri));
	}

	// ------------------------------------------------------- library functions

	@Test
	void unusedLibraryFunctionIsMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				library function Twice(x number) number
				""");

		assertNoIssues();

		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertEquals(1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		Assertions.assertEquals("Library function 'Twice' is never used", diagnostic.getMessage());
		Assertions.assertEquals(DiagnosticSeverity.Hint, diagnostic.getSeverity());
		Assertions.assertTrue(diagnostic.getTags().contains(DiagnosticTag.Unnecessary));
	}

	/**
	 * {@code RosettaSymbolReference.symbol} — a library function is a {@code RosettaCallableWithArgs}, so it
	 * is called exactly like a {@code func}.
	 */
	@Test
	void calledLibraryFunctionIsNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				library function Twice(x number) number

				func Caller:
					[suppressUnused]
					output: result number (1..1)
					set result: Twice(21)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	// ------------------------------------------------- built-in kinds and guards

	/**
	 * {@code basicType} and {@code recordType} are candidates like any other named root element — it is the
	 * built-in <em>files</em> that are excluded, not these kinds.
	 */
	@Test
	void unusedBasicAndRecordTypesAreMarkedAsUnnecessary() {
		String uri = createModel("model.rosetta", """
				namespace test

				basicType money

				recordType interval
				{
					low int
					high int
				}
				""");

		assertNoIssues();
		Assertions.assertEquals(
				List.of("Basic type 'money' is never used", "Record type 'interval' is never used"),
				unusedMarkers(uri));
	}

	@Test
	void basicAndRecordTypesUsedAsAttributeTypesAreNotFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				basicType money

				recordType interval
				{
					low int
					high int
				}

				type Quote:
					[rootType]
					price money (1..1)
					window interval (1..1)
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * A {@code metaType} is never the target of a cross-reference — it is resolved by name through the index
	 * — so the walk could never see one as used and a marker on it would be permanent. It is therefore
	 * excluded from candidacy outright, unlike every other named root element.
	 */
	@Test
	void metaTypeIsNeverFlagged() {
		String uri = createModel("model.rosetta", """
				namespace test

				metaType reference string
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkers(uri));
	}

	/**
	 * A {@code report} is the one root element with no name, so it is not a candidate: there would be nowhere
	 * to put the marker. Nothing can reference a report either, so without the naming rule every report in
	 * every model would be flagged.
	 */
	@Test
	void reportIsNeverFlagged() {
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

	/**
	 * The built-in models are read-only and none of the kinds they declare can carry {@code [suppressUnused]},
	 * so every builtin a given model happens not to use would otherwise be marked permanently. Asserted
	 * across every file the server published diagnostics for, since the builtins are part of the resource set
	 * and do get validated.
	 */
	@Test
	void builtinDeclarationsAreNeverFlagged() {
		createModel("model.rosetta", """
				namespace test

				type Foo:
					[rootType]
					a string (1..1)
				""");

		assertNoIssues();

		List<String> builtinMarkers = getDiagnostics().entrySet().stream()
				.filter(e -> e.getKey().endsWith("basictypes.rosetta") || e.getKey().endsWith("annotations.rosetta"))
				.flatMap(e -> e.getValue().stream())
				.filter(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary))
				.map(Diagnostic::getMessage)
				.sorted()
				.toList();
		Assertions.assertEquals(List.of(), builtinMarkers);
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
	 * Asserts the model is free of errors, for a model that legitimately carries a warning and so cannot use
	 * {@code assertNoIssues()}. Without it a broken AST would silently change what the marker assertions
	 * measure.
	 */
	private void assertNoErrors(String uri) {
		List<String> errors = getDiagnostics().get(uri).stream()
				.filter(d -> d.getSeverity() == DiagnosticSeverity.Error)
				.map(Diagnostic::getMessage)
				.toList();
		Assertions.assertEquals(List.of(), errors);
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
