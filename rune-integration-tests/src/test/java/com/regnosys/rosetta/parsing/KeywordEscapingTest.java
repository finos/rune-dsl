package com.regnosys.rosetta.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

/**
 * A name that collides with a keyword is written with a leading caret, e.g. `^type`. These tests
 * cover where that escape has to be understood: reading a model, writing one back out, and the
 * names in between.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class KeywordEscapingTest {
	@Inject
	private IValueConverterService valueConverter;
	@Inject
	private RosettaGrammarAccess grammarAccess;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void testEveryNameSegmentIsUnescaped() {
		assertEquals("type", valueConverter.toValue("^type", "ValidID", null));
		assertEquals("foo.type.bar", valueConverter.toValue("^foo.^type.^bar", "QualifiedName", null));
		assertEquals("foo.type.*", valueConverter.toValue("^foo.^type.*", "QualifiedNameWithWildcard", null));
		assertEquals("min", valueConverter.toValue("^min", "TypeParameterValidID", null));
	}

	@Test
	void testEveryNameSegmentIsEscaped() {
		assertEquals("^type", valueConverter.toString("type", "ValidID"));
		assertEquals("foo.^type.bar", valueConverter.toString("foo.type.bar", "QualifiedName"));
		assertEquals("foo.^type.*", valueConverter.toString("foo.type.*", "QualifiedNameWithWildcard"));
		assertEquals("^type", valueConverter.toString("type", "TypeParameterValidID"));
	}

	@Test
	void testKeywordsThatAreValidNamesAreNotEscaped() {
		// `ValidID` accepts these as a name, so a caret would only add noise.
		assertEquals("condition", valueConverter.toString("condition", "ValidID"));
		assertEquals("foo.version.bar", valueConverter.toString("foo.version.bar", "QualifiedName"));
		// ... and `TypeParameterValidID` accepts two more.
		assertEquals("min", valueConverter.toString("min", "TypeParameterValidID"));
		assertEquals("max", valueConverter.toString("max", "TypeParameterValidID"));
		// `min` is not a name anywhere else, though.
		assertEquals("^min", valueConverter.toString("min", "ValidID"));
	}

	@Test
	void testNameThatCannotBeWrittenIsRejected() {
		// Nothing that reaches a value converter should end up in the document unchecked.
		assertThrows(ValueConverterException.class, () -> valueConverter.toString("only-element", "ValidID"));
		assertThrows(ValueConverterException.class, () -> valueConverter.toString("foo.only-element", "QualifiedName"));
		// A wildcard is a name only at the end of an import.
		assertThrows(ValueConverterException.class, () -> valueConverter.toString("foo.*", "QualifiedName"));
		assertThrows(ValueConverterException.class,
				() -> valueConverter.toString("foo.*.bar", "QualifiedNameWithWildcard"));
	}

	@Test
	void testWhitespaceAroundADotIsNotPartOfTheName() {
		// The grammar allows it, so the name a segment stands for has to be read out of it.
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
				namespace test . ^namespace

				type Foo:
				""");
		assertEquals("test.namespace", model.getName());
	}

	@Test
	void testEscapedNamespaceSegmentHoldsTheUnescapedName() {
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
				namespace test.^namespace.^type

				type Foo:
				""");
		assertEquals("test.namespace.type", model.getName());
	}

	@Test
	void testCanReferToAnEscapedNamespaceByItsQualifiedName() {
		List<RosettaModel> models = modelHelper.parseRosettaWithNoErrors("""
				namespace ^namespace.^type

				type Foo:
				""", """
				namespace test

				type Bar extends ^namespace.^type.Foo:
				""");
		assertNoUnresolvedReferences(models.get(1));
	}

	@Test
	void testCanImportAnEscapedNamespace() {
		List<RosettaModel> models = modelHelper.parseRosettaWithNoErrors("""
				namespace ^namespace.^type

				type Foo:
				""", """
				namespace test

				import ^namespace.^type.*

				type Bar extends Foo:
				""");
		assertEquals("namespace.type.*", models.get(1).getImports().get(0).getImportedNamespace());
		assertNoUnresolvedReferences(models.get(1));
	}

	@Test
	void testCanEscapeATypeParameter() {
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias SmallNumber: number(^min: 1, ^max: 10)
				""");
		assertNoUnresolvedReferences(model);
	}

	/**
	 * The rules a name can be written with, and what each of them has to do with a keyword. When
	 * the grammar grows a new one this assertion fails, which is the reminder to register a value
	 * converter for it in `RosettaValueConverterService` - without one Xtext passes the text
	 * through unchanged and the caret ends up in the name, as it did for
	 * `QualifiedNameWithWildcard` and `TypeParameterValidID`.
	 */
	@Test
	void testEveryRuleThatCanWriteANameEscapesKeywords() {
		assertEquals(
				Set.of("ValidID", "TypeParameterValidID", "QualifiedName", "QualifiedNameWithWildcard",
						// Not a name: the trailing `ID` of `BigDecimal` is an exponent suffix.
						"BigDecimal"),
				rulesThatCanYieldAnIdentifier());

		for (String rule : List.of("ValidID", "TypeParameterValidID", "QualifiedName", "QualifiedNameWithWildcard")) {
			assertEquals("^type", valueConverter.toString("type", rule), rule + " must escape a keyword name");
			assertEquals("type", valueConverter.toValue("^type", rule, null), rule + " must unescape a name");
		}
	}

	private Set<String> rulesThatCanYieldAnIdentifier() {
		TerminalRule idRule = grammarAccess.getIDRule();
		return GrammarUtil.allParserRules(grammarAccess.getGrammar()).stream()
				.filter(GrammarUtil::isDatatypeRule)
				.filter(rule -> canYieldAnIdentifier(rule, idRule, new HashSet<>()))
				.map(AbstractRule::getName)
				.collect(Collectors.toSet());
	}

	private static boolean canYieldAnIdentifier(AbstractRule rule, AbstractRule idRule, Set<AbstractRule> visited) {
		if (!visited.add(rule)) {
			return false;
		}
		return EcoreUtil2.getAllContentsOfType(rule, RuleCall.class).stream()
				.map(RuleCall::getRule)
				.anyMatch(called -> called == idRule
						|| called instanceof ParserRule && canYieldAnIdentifier(called, idRule, visited));
	}

	private static void assertNoUnresolvedReferences(RosettaModel model) {
		EcoreUtil.resolveAll(model.eResource());
		model.eResource().getAllContents().forEachRemaining(object ->
				object.eCrossReferences().forEach(reference ->
						assertFalse(reference.eIsProxy(),
								"Unresolved reference from " + object.eClass().getName() + ": " + reference)));
	}
}
