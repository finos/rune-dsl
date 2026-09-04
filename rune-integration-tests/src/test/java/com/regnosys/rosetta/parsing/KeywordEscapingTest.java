package com.regnosys.rosetta.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

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

	// A single segment is covered for every rule by `testEveryRuleThatCanWriteANameEscapesKeywords`;
	// these two are about the segments a qualified name is made of.
	@Test
	void testEveryNameSegmentIsUnescaped() {
		assertEquals("foo.type.bar", valueConverter.toValue("^foo.^type.^bar", "QualifiedName", null));
		assertEquals("foo.type.*", valueConverter.toValue("^foo.^type.*", "QualifiedNameWithWildcard", null));
	}

	@Test
	void testEveryNameSegmentIsEscaped() {
		assertEquals("foo.^type.bar", valueConverter.toString("foo.type.bar", "QualifiedName"));
		assertEquals("foo.^type.*", valueConverter.toString("foo.type.*", "QualifiedNameWithWildcard"));
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
		assertThrows(ValueConverterException.class, () -> valueConverter.toString("*", "QualifiedNameWithWildcard"));
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
		modelHelper.parseRosettaWithNoIssues("""
				namespace ^namespace.^type

				type Foo:
				""", """
				namespace test

				type Bar extends ^namespace.^type.Foo:
				""");
	}

	@Test
	void testCanImportAnEscapedNamespace() {
		List<RosettaModel> models = modelHelper.parseRosettaWithNoIssues("""
				namespace ^namespace.^type

				type Foo:
				""", """
				namespace test

				import ^namespace.^type.*

				type Bar extends Foo:
				""");
		assertEquals("namespace.type.*", models.get(1).getImports().get(0).getImportedNamespace());
	}

	@Test
	void testCanReferToAnEscapedRuleFromARuleReference() {
		// An annotation refers to a rule by qualified name, and `rule` is a keyword, so both the
		// namespace and the rule's own name can need escaping.
		modelHelper.parseRosettaWithNoIssues("""
				namespace my.^rule

				reporting rule ^rule from string:
					item
				""", """
				namespace test

				type Foo:
					attr string (1..1)
						[ruleReference my.^rule.^rule]
				""");
	}

	@Test
	void testCanReferToEscapedDocumentElementsFromADocReference() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace my.^rule

				body Authority ^body
				corpus Regulation "cn" ^corpus
				segment ^segment
				""", """
				namespace test

				type Foo:
					[docReference my.^rule.^body my.^rule.^corpus my.^rule.^segment "x"]
					attr string (1..1)
				""");
	}

	@Test
	void testCanEscapeATypeParameter() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias SmallNumber: number(^min: 1, ^max: 10)
				""");
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
		Set<String> nameRules = new HashSet<>(rulesThatCanYieldAnIdentifier());
		// Not a name: the trailing `ID` of `BigDecimal` is an exponent suffix.
		assertTrue(nameRules.remove("BigDecimal"));
		assertEquals(Set.of("ValidID", "TypeParameterValidID", "QualifiedName", "QualifiedNameWithWildcard"),
				nameRules);

		for (String rule : nameRules) {
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

}
