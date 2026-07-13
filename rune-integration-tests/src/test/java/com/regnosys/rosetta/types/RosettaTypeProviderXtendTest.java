package com.regnosys.rosetta.types;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ARITHMETIC_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.COMPARISON_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.EQUALITY_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.LIST_LITERAL;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.LOGICAL_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_CONDITIONAL_EXPRESSION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_ONLY_EXISTS_EXPRESSION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

// Should be moved over to Java RosettaTypeProviderTest
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(Lifecycle.PER_CLASS)
public class RosettaTypeProviderXtendTest {

	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private ModelHelper modelHelper;
	@Inject
	private ValidationTestHelper validationTestHelper;
	@Inject
	private ExpressionParser expressionParser;
	@Inject
	private TypeFactory typeFactory;
	@Inject
	private RObjectFactory objectFactory;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RBuiltinTypeService builtins;

	private RMetaAttribute scheme;

	@BeforeAll
	void setup() {
		scheme = new RMetaAttribute("scheme", builtins.UNCONSTRAINED_STRING);
	}

	private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti,
			List<RosettaModel> context, List<String> attributes) {
		assertIsValidWithType(expressionParser.parseExpression(expr, context, attributes), expr, expectedType, expectedIsMulti);
	}

	private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti,
			List<String> attributes) {
		assertIsValidWithType(expressionParser.parseExpression(expr, attributes), expr, expectedType, expectedIsMulti);
	}

	private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		assertIsValidWithType(expressionParser.parseExpression(expr), expr, expectedType, expectedIsMulti);
	}

	private void assertIsValidWithType(RosettaExpression expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		assertIsValidWithType(expr, NodeModelUtils.findActualNodeFor(expr).getText(), expectedType, expectedIsMulti);
	}

	private void assertIsValidWithType(RosettaExpression expr, CharSequence originalExpression,
			RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		validationTestHelper.assertNoIssues(expr);
		RMetaAnnotatedType actual = typeProvider.getRMetaAnnotatedType(expr);

		assertEquals(expectedType, actual, "Expression: " + originalExpression);
		if (expectedIsMulti) {
			assertTrue(cardinalityProvider.isMulti(expr), "Expected multi cardinality. Expression: " + originalExpression);
		} else {
			assertFalse(cardinalityProvider.isMulti(expr), "Expected single cardinality. Expression: " + originalExpression);
		}
	}

	@Test
	void testLiteralTypeInference() {
		assertIsValidWithType("False", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("\"Some string\"", typeFactory.stringWithNoMeta(11, 11), false);
		assertIsValidWithType("3.14", typeFactory.numberWithNoMeta(3, 2, "3.14", "3.14"), false);
		assertIsValidWithType("1", typeFactory.intWithNoMeta(1, "1", "1"), false);
		assertIsValidWithType("empty", builtins.NOTHING_WITH_ANY_META, false);
	}

	@Test
	void testVariableTypeInference() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				func TestVar:
					[suppressWarnings unused]
					output: result number (1..4)
					alias c: if True then 42 else -1/12
					add result:
						c

				func TestImplicitVar:
					[suppressWarnings unused]
					output: result int (3..3)
					add result:
						[1, 2, 3] extract item + 1
				""");
		assertIsValidWithType("a", RMetaAnnotatedType.withMeta(builtins.UNCONSTRAINED_INT, List.of(scheme)), true,
				List.of("a int (2..4) [metadata scheme]"));
		assertIsValidWithType("b", builtins.BOOLEAN_WITH_NO_META, false, List.of("b boolean (1..1)"));

		Function testVar = (Function) context.getElements().get(0);
		assertIsValidWithType(testVar.getOperations().get(0).getExpression(), builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, false);

		Function testImplicitVar = (Function) context.getElements().get(1);
		MapOperation mapOperation = (MapOperation) testImplicitVar.getOperations().get(0).getExpression();
		ArithmeticOperation body = (ArithmeticOperation) mapOperation.getFunction().getBody();
		assertIsValidWithType(body.getLeft(), typeFactory.intWithNoMeta(1, "1", "3"), false);
	}

	@Test
	void testLogicalOperationTypeInference() {
		assertIsValidWithType("True or False", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("True and False", builtins.BOOLEAN_WITH_NO_META, false);
	}

	@Test
	void testLogicalOperationTypeChecking() {
		validationTestHelper.assertError(expressionParser.parseExpression("1 or False"), LOGICAL_OPERATION, null,
				"Expected type `boolean`, but got `int` instead. Cannot use `int` with operator `or`");
		validationTestHelper.assertError(expressionParser.parseExpression("True or 3.14"), LOGICAL_OPERATION, null,
				"Expected type `boolean`, but got `number` instead. Cannot use `number` with operator `or`");
		validationTestHelper.assertError(expressionParser.parseExpression("a or False", List.of("a boolean (1..2)")),
				LOGICAL_OPERATION, null,
				"Expecting single cardinality. The `or` operator requires a single cardinality input");
	}

	@Test
	void testEqualityOperationTypeInference() {
		assertIsValidWithType("[2, 3] = [6.0, 7, 8]", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("[2, 3] <> [6.0, 7, 8]", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("[1, 3] all = 5.0", builtins.BOOLEAN_WITH_NO_META, false);
		// TODO?
		// assertIsValidWithType("empty all <> 5.0", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("[1, 3] any = 5.0", builtins.BOOLEAN_WITH_NO_META, false);

		assertIsValidWithType("a = 1", builtins.BOOLEAN_WITH_NO_META, false, List.of("a int (0..1)"));
	}

	@Test
	void testEqualityOperationTypeChecking() {
		validationTestHelper.assertError(expressionParser.parseExpression("1 = True"), EQUALITY_OPERATION, null,
				"Types `int` and `boolean` are not comparable");
		// TODO?
		// "empty = True" -> "Cannot compare an empty value to a single value, ..."
		// "[1, 2] = [3, 4, 5]" -> "Cannot compare a list with 2 items to a list with 3 items, ..."
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] <> [True, False, False]"),
				EQUALITY_OPERATION, null, "Types `int` and `boolean` are not comparable");

		validationTestHelper.assertError(expressionParser.parseExpression("1 = True"), EQUALITY_OPERATION, null,
				"Types `int` and `boolean` are not comparable");
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 3] any <> a", List.of("a int (1..2)")),
				EQUALITY_OPERATION, null, "Expecting single cardinality");
		// "[1, 2] all = empty" -> "Expected a single value, but got an empty value instead"
		// "empty any = empty" -> "Expected a single value, but got an empty value instead"
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] all = [1, 2]"), EQUALITY_OPERATION,
				null, "Expecting single cardinality");
		validationTestHelper.assertError(expressionParser.parseExpression("5 any <> [1, 2]"), EQUALITY_OPERATION, null,
				"Expecting multi cardinality. Did you mean to flip around the operands of the `<>` operator?");
		// "[3.0] any <> 5" -> "The cardinality operator `any` is redundant when comparing two single values"
	}

	// TODO: test arithmetic and comparisons with dates/times/etc
	@Test
	void testArithmeticOperationTypeInference() {
		assertIsValidWithType("3 + 4", typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(7)),
				Optional.of(BigInteger.valueOf(7))), false);
		assertIsValidWithType("3.0 + 4", typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1),
				Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
		assertIsValidWithType("3 + 4.0", typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1),
				Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
		assertIsValidWithType("3.0 + 4.0", typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1),
				Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
		assertIsValidWithType("\"ab\" + \"cd\"", typeFactory.stringWithNoMeta(4, 4), false);

		assertIsValidWithType("3 - 4", typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(-1)),
				Optional.of(BigInteger.valueOf(-1))), false);
		assertIsValidWithType("3 - 4.0", typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1),
				Optional.of(new BigDecimal("-1")), Optional.of(new BigDecimal("-1")), Optional.empty()), false);

		assertIsValidWithType("3 * 4", typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(12)),
				Optional.of(BigInteger.valueOf(12))), false);
		assertIsValidWithType("3.0 * 4", typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1),
				Optional.of(new BigDecimal("12")), Optional.of(new BigDecimal("12")), Optional.empty()), false);

		assertIsValidWithType("3 / 4", builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, false);
	}

	@Test
	void testArithmeticOperationTypeChecking() {
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] + 3"), ARITHMETIC_OPERATION, null,
				"Expecting single cardinality. The `+` operator requires a single cardinality input");
		// TODO
		// "empty - 3" -> "Expected a single value, but got an empty value instead."
		validationTestHelper.assertError(expressionParser.parseExpression("1.5 * False"), ARITHMETIC_OPERATION, null,
				"Expected type `number`, but got `boolean` instead. Cannot use `boolean` with operator `*`");
		validationTestHelper.assertError(expressionParser.parseExpression("\"ab\" + 3"), ARITHMETIC_OPERATION, null,
				"Expected type `string`, but got `int` instead. Cannot add `int` to a `string`");
		validationTestHelper.assertError(expressionParser.parseExpression("a + 5", List.of("a int (1..2)")),
				ARITHMETIC_OPERATION, null,
				"Expecting single cardinality. The `+` operator requires a single cardinality input");
	}

	@Test
	void testComparisonOperationTypeInference() {
		assertIsValidWithType("1 < 2", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("3 > 3.14", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("-5.1 <= 42", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("-3.14 >= 3.14", builtins.BOOLEAN_WITH_NO_META, false);

		assertIsValidWithType("[1, 2] any < 5", builtins.BOOLEAN_WITH_NO_META, false);
		// TODO?
		// assertIsValidWithType("empty all > 5", builtins.BOOLEAN_WITH_NO_META, false);
	}

	@Test
	void testComparisonOperationTypeChecking() {
		// TODO: support date, zonedDateTime and `time`?
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] < 3"), COMPARISON_OPERATION, null,
				"Expecting single cardinality. Did you mean to use `all` or `any` in front of the `<` operator?");
		// TODO
		// "empty > 3" -> "Expected a single value, but got an empty value instead."
		validationTestHelper.assertError(expressionParser.parseExpression("1.5 <= False"), COMPARISON_OPERATION, null,
				"Expected type `number`, but got `boolean` instead. Cannot compare a `boolean` to a `number`");

		validationTestHelper.assertError(expressionParser.parseExpression("a < 5", List.of("a int (1..2)")),
				COMPARISON_OPERATION, null,
				"Expecting single cardinality. Did you mean to use `all` or `any` in front of the `<` operator?");
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] any < a", List.of("a int (1..2)")),
				COMPARISON_OPERATION, null, "Expecting single cardinality");
		// "[1, 2] all >= empty" -> "Expected a single value, but got an empty value instead"
		validationTestHelper.assertError(expressionParser.parseExpression("empty any < empty"), COMPARISON_OPERATION,
				null, "Expecting multi cardinality. Did you mean to remove the `any` modifier on the `<` operator?");
		validationTestHelper.assertError(expressionParser.parseExpression("[1, 2] all > [1, 2]"), COMPARISON_OPERATION,
				null, "Expecting single cardinality");
		validationTestHelper.assertError(expressionParser.parseExpression("5 any <= [1, 2]"), COMPARISON_OPERATION, null,
				"Expecting single cardinality. Did you mean to flip around the operands of the `<=` operator?");
		validationTestHelper.assertError(expressionParser.parseExpression("5 all >= 1"), COMPARISON_OPERATION, null,
				"Expecting multi cardinality. Did you mean to remove the `all` modifier on the `>=` operator?");
	}

	@Test
	void testConditionalExpressionTypeInference() {
		assertIsValidWithType("if True then [1, 2] else [3.0, 4.0, 5.0, 6.0]",
				typeFactory.numberWithNoMeta(2, 1, "1", "6"), true);
	}

	@Test
	void testConditionalExpressionTypeChecking() {
		validationTestHelper.assertError(expressionParser.parseExpression("if [True, False] then 1 else 2"),
				ROSETTA_CONDITIONAL_EXPRESSION, null,
				"Expecting single cardinality. The condition of an if-then-else expression should be single cardinality");
		// TODO
		// "if empty then 1 else 2" -> "Expected a single value, but got an empty value instead."
		validationTestHelper.assertError(expressionParser.parseExpression("if True then 1 else False"),
				ROSETTA_CONDITIONAL_EXPRESSION, null, "Types `int` and `boolean` do not have a common supertype");
		validationTestHelper.assertError(expressionParser.parseExpression("if True then [1, 2, 3] else [False, True]"),
				ROSETTA_CONDITIONAL_EXPRESSION, null, "Types `int` and `boolean` do not have a common supertype");
	}

	@Test
	void testListLiteralTypeInference() {
		assertIsValidWithType("[]", builtins.NOTHING_WITH_ANY_META, false);
		assertIsValidWithType("[2, 4.5, 7, -3.14]", typeFactory.numberWithNoMeta(3, 2, "-3.14", "7"), true);
		assertIsValidWithType("[2, [1, 2], -3.14]", typeFactory.numberWithNoMeta(3, 2, "-3.14", "2"), true);
	}

	@Test
	void testListLiteralTypeChecking() {
		validationTestHelper.assertError(expressionParser.parseExpression("[1, True]"), LIST_LITERAL, null,
				"Types `int` and `boolean` do not have a common supertype");
	}

	@Test
	void testFunctionCallTypeInference() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				func SomeFunc:
					[suppressWarnings unused]
					inputs:
						a int (1..1)
						b boolean (2..4)
					output: result number (3..5)
					add result:
						[1.0, 2.0, 3.0]
				""");
		assertIsValidWithType("SomeFunc(42, [True, False, True])", builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, true,
				List.of(context), List.of());
	}

	@Test
	void testFunctionCallTypeChecking() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				func SomeFunc:
					[suppressWarnings unused]
					inputs:
					    a int (1..1)
					    b boolean (2..4)
					output: result int (1..1)
					set result:
						42
				""");

		validationTestHelper.assertError(
				expressionParser.parseExpression("SomeFunc(1, [False, True], True)", List.of(context)),
				ROSETTA_SYMBOL_REFERENCE, null, "Expected 2 arguments, but got 3 instead");
		validationTestHelper.assertError(
				expressionParser.parseExpression("SomeFunc(1, [2, 3])", List.of(context)), ROSETTA_SYMBOL_REFERENCE,
				null, "Expected type `boolean`, but got `int` instead. Cannot assign `int` to input `b`");
		// TODO
		// "SomeFunc(1, [False, True, False, False, True])"
		//   -> "Expected a list with 2 to 4 items, but got a list with 5 items instead"
	}

	@Test
	void testProjectionTypeInference() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				type A:
					x int (1..1)
					y number (0..*)
					z boolean (3..7)
				""");
		assertIsValidWithType("a -> x", builtins.UNCONSTRAINED_INT_WITH_NO_META, false, List.of(context), List.of("a A (1..1)"));
		assertIsValidWithType("a -> y", builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, true, List.of(context), List.of("a A (1..1)"));
		assertIsValidWithType("a -> z", builtins.BOOLEAN_WITH_NO_META, true, List.of(context), List.of("a A (1..1)"));
		assertIsValidWithType("a -> x", builtins.UNCONSTRAINED_INT_WITH_NO_META, true, List.of(context), List.of("a A (2..5)"));
		assertIsValidWithType("a -> y", builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, true, List.of(context), List.of("a A (1..1)"));
		assertIsValidWithType("a -> z", builtins.BOOLEAN_WITH_NO_META, true, List.of(context), List.of("a A (1..1)"));
	}

	@Test
	void testEnumTypeInference() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				enum A:
					V1
					V2

				func Test:
					[suppressWarnings unused]
					output: result A (1..1)
					set result:
						A -> V1
				""");
		REnumType a = objectFactory.buildREnumType((RosettaEnumeration) context.getElements().get(0));
		assertIsValidWithType("A -> V1", RMetaAnnotatedType.withNoMeta(a), false, List.of(context), List.of());
	}

	@Test
	void testExistsTypeInference() {
		assertIsValidWithType("a exists", builtins.BOOLEAN_WITH_NO_META, false, List.of("a int (0..1)"));
		assertIsValidWithType("a exists", builtins.BOOLEAN_WITH_NO_META, false, List.of("a int (0..3)"));
	}

	@Test
	void testExistsTypeChecking() {
		// TODO
		// "empty exists" -> "Expected an optional value, but got an empty value instead."
		// "42 exists" -> "Expected an optional value, but got a single value instead."
		// "(if True then 42 else [1, 2, 3, 4, 5]) exists"
		//   -> "Expected an optional value, but got a list with 1 to 5 items instead."
	}

	@Test
	void testAbsentTypeInference() {
		assertIsValidWithType("a is absent", builtins.BOOLEAN_WITH_NO_META, false, List.of("a int (0..1)"));
		assertIsValidWithType("a is absent", builtins.BOOLEAN_WITH_NO_META, false, List.of("a int (0..3)"));
	}

	@Test
	void testAbsentTypeChecking() {
		// TODO
		// "empty is absent" -> "Expected an optional value, but got an empty value instead."
		// "42 is absent" -> "Expected an optional value, but got a single value instead."
		// "(if True then 42 else [1, 2, 3, 4, 5]) is absent"
		//   -> "Expected an optional value, but got a list with 1 to 5 items instead."
	}

	@Test
	void testCountTypeInference() {
		RMetaAnnotatedType positiveInt = typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty());
		assertIsValidWithType("empty count", positiveInt, false);
		assertIsValidWithType("42 count", positiveInt, false);
		assertIsValidWithType("[1, 2, 3] count", positiveInt, false);
		assertIsValidWithType("(if True then empty else [1, 2, 3]) count", positiveInt, false);
	}

	@Test
	void testOnlyExistsTypeInference() {
		RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				type A:
					x int (0..1)
					y number (0..3)
					z boolean (0..*)

					condition C:
						x only exists and (x, y) only exists
				""");

		LogicalOperation condition = (LogicalOperation) ((Data) context.getElements().get(0)).getConditions().get(0).getExpression();
		assertIsValidWithType(condition.getLeft(), builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType(condition.getRight(), builtins.BOOLEAN_WITH_NO_META, false);

		assertIsValidWithType("a -> x only exists", builtins.BOOLEAN_WITH_NO_META, false, List.of(context), List.of("a A (1..1)"));
		assertIsValidWithType("(a -> x, a -> y) only exists", builtins.BOOLEAN_WITH_NO_META, false, List.of(context), List.of("a A (1..1)"));
	}

	@Test
	void testOnlyExistsTypeChecking() {
		RosettaModel model = modelHelper.parseRosetta("""
				namespace test

				type Foo:
					bar int (1..1)
					baz boolean (0..1)

					condition X:
						baz only exists

				type A:
					x Foo (0..1)
					y number (0..3)
					z boolean (0..*)

					condition C1:
						(x -> baz, y) only exists and (y, x -> baz) only exists
					condition C2:
						(x, x) only exists and (x -> baz, x -> baz) only exists

				func Test:
					inputs:
					    a A (1..1)
					    foo Foo (1..1)
					    b A (2..3)
					    c A (0..1)
					output: result boolean (0..*)
					add result:
						b -> x only exists
					add result:
						c only exists
					add result:
						(a -> x -> baz, a -> x) only exists
					add result:
						foo -> baz only exists
				""");

		Data foo = (Data) model.getElements().get(0);
		validationTestHelper.assertError(foo.getConditions().get(0).getExpression(), ROSETTA_SYMBOL_REFERENCE, null,
				"Operator `only exists` is not supported for type `Foo`. All attributes of input type should be optional");

		Data a = (Data) model.getElements().get(1);
		LogicalOperation c1 = (LogicalOperation) a.getConditions().get(0).getExpression();
		validationTestHelper.assertError(c1.getLeft(), ROSETTA_SYMBOL_REFERENCE, null, "All parent paths must be equal");
		validationTestHelper.assertError(c1.getRight(), ROSETTA_SYMBOL_REFERENCE, null, "All parent paths must be equal");
		LogicalOperation c2 = (LogicalOperation) a.getConditions().get(1).getExpression();
		validationTestHelper.assertError(c2.getLeft(), ROSETTA_ONLY_EXISTS_EXPRESSION, null, "Duplicate attribute");
		validationTestHelper.assertError(c2.getRight(), ROSETTA_ONLY_EXISTS_EXPRESSION, null, "Duplicate attribute");

		Function test = (Function) model.getElements().get(2);
		validationTestHelper.assertError(test.getOperations().get(0).getExpression(), ROSETTA_SYMBOL_REFERENCE, null,
				"Expecting single cardinality. The `only exists` operator requires a single cardinality input");
		validationTestHelper.assertError(test.getOperations().get(1).getExpression(), ROSETTA_ONLY_EXISTS_EXPRESSION,
				null, "Object must have a parent object");
		validationTestHelper.assertError(test.getOperations().get(2).getExpression(), ROSETTA_SYMBOL_REFERENCE, null,
				"All parent paths must be equal");
		validationTestHelper.assertError(test.getOperations().get(3).getExpression(), ROSETTA_SYMBOL_REFERENCE, null,
				"Operator `only exists` is not supported for type `Foo`. All attributes of input type should be optional");
	}

	@Test
	void testOnlyElementTypeInference() {
		assertIsValidWithType("(if True then 0 else [1, 2]) only-element", typeFactory.intWithNoMeta(1, "0", "2"), false);
		assertIsValidWithType("(if True then empty else [True, False]) only-element", builtins.BOOLEAN_WITH_NO_META, false);
		assertIsValidWithType("(if True then 0 else [1, 2, 3, 42.0]) only-element",
				typeFactory.numberWithNoMeta(3, 1, "0", "42.0"), false);
	}

	@Test
	void testOnlyElementTypeChecking() {
		// TODO
		// "empty only-element" -> "Expected a list with 1 to 2 items, but got an empty value instead."
		// "42 only-element" -> "Expected a list with 1 to 2 items, but got a single value instead."
		// "[1, 2] only-element" -> "Expected a list with 1 to 2 items, but got a list with 2 items instead."
		// "(if True then empty else 42) only-element"
		//   -> "Expected a list with 1 to 2 items, but got an optional value instead."
	}

	@Test
	void testTypeAliasJoin() {
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias maxNString(n int): string(minLength: 1, maxLength: n)
				typeAlias max3String: maxNString(n: 3)
				typeAlias max4String: maxNString(n: 4)

				func Test:
					[suppressWarnings unused]
					inputs:
						s1 max3String (1..1)
						s2 max4String (1..1)
						s3 maxNString(n: 4) (1..1)
					output: result string (0..*)
					add result: if True then s1 else s2
					add result: if True then s2 else s2
					add result: if True then s2 else s3
				""");
		Function test = (Function) model.getElements().get(model.getElements().size() - 1);
		RMetaAnnotatedType max4String = RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(test.getInputs().get(1).getTypeCall()));
		RMetaAnnotatedType maxNString = RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(test.getInputs().get(2).getTypeCall()));

		assertIsValidWithType(test.getOperations().get(0).getExpression(), maxNString, false);
		assertIsValidWithType(test.getOperations().get(1).getExpression(), max4String, false);
		assertIsValidWithType(test.getOperations().get(2).getExpression(), maxNString, false);
	}

	@Test
	void shouldCoerceStringToParameterizedString() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				func Test:
					[suppressWarnings unused]
					inputs: str string (1..1)
					output: max3String string(minLength: 1, maxLength: 3) (1..1)
					set max3String: str
				""");
	}

	@Test
	void shouldCoerceParameterizedStringToString() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				func Test:
					[suppressWarnings unused]
					inputs: max3String string(minLength: 1, maxLength: 3) (1..1)
					output: str string (1..1)
					set str: max3String
				""");
	}

	@Test
	void shouldCoerceStringToStringTypeAlias() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias Max3String: string(minLength: 1, maxLength: 3)

				func Test:
					[suppressWarnings unused]
					inputs: str string (1..1)
					output: max3String Max3String (1..1)
					set max3String: str
				""");
	}

	@Test
	void shouldCoerceStringTypeAliasToString() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias Max3String: string(minLength: 1, maxLength: 3)

				func Test:
					[suppressWarnings unused]
					inputs: max3String Max3String (1..1)
					output: str string (1..1)
					set str: max3String
				""");
	}

	@Test
	void shouldCoerceDifferentParameterizedStrings() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				func Test:
					[suppressWarnings unused]
					inputs: max10String string(minLength: 1, maxLength: 10) (1..1)
					output: max3String string(minLength: 1, maxLength: 3) (1..1)
					set max3String: max10String
				""");
	}

	@Test
	void shouldCoerceDifferentTypeAliases() {
		modelHelper.parseRosettaWithNoIssues("""
				namespace test

				typeAlias Max10String: string(minLength: 1, maxLength: 10)
				typeAlias Max3String: string(minLength: 1, maxLength: 3)

				func Test:
					[suppressWarnings unused]
					inputs: max10String Max10String (1..1)
					output: max3String Max3String (1..1)
					set max3String: max10String
				""");
	}

	@Test
	void testAttributeSameNameAsAnnotationTest() {
		RosettaModel model = modelHelper.parseRosetta("""
				type A:
					[rootType]
					rootType string (0..1)

					condition C:
						rootType exists
				""");

		validationTestHelper.assertNoIssues(model);
	}

	@Test
	void testBinaryExpressionCommonType() {
		List<Function> funcs = modelHelper.parseRosettaWithNoErrors("""
				isEvent root Foo;

				type Foo:
					iBar int (0..*)
					nBar number (0..*)
					nBuz number (0..*)

				func Qualify_AllNumber:
					[qualification BusinessEvent]
					inputs: foo Foo (1..1)
					output: is_event boolean (1..1)
					set is_event:
						[foo -> nBar, foo -> nBuz] contains 4.0

				func Qualify_MixedNumber:
					[qualification BusinessEvent]
					inputs: foo Foo (1..1)
					output: is_event boolean (1..1)
					set is_event:
						[foo -> nBar, foo -> iBar] contains 4.0

				func Qualify_IntOnly:
					[qualification BusinessEvent]
					inputs: foo Foo (1..1)
					output: is_event boolean (1..1)
					set is_event:
						foo -> iBar any = 4.0
				""").getElements().stream()
				.filter(Function.class::isInstance)
				.map(Function.class::cast)
				.toList();

		Function allNumber = funcs.stream().filter(f -> "Qualify_AllNumber".equals(f.getName())).findFirst().orElseThrow();
		assertEquals("number", typeProvider.getRMetaAnnotatedType(
				((RosettaContainsExpression) allNumber.getOperations().get(0).getExpression()).getLeft()).getRType().getName());
		Function mixed = funcs.stream().filter(f -> "Qualify_MixedNumber".equals(f.getName())).findFirst().orElseThrow();
		assertEquals("number", typeProvider.getRMetaAnnotatedType(
				((RosettaContainsExpression) mixed.getOperations().get(0).getExpression()).getLeft()).getRType().getName());
		Function intOnly = funcs.stream().filter(f -> "Qualify_IntOnly".equals(f.getName())).findFirst().orElseThrow();
		assertEquals("int", typeProvider.getRMetaAnnotatedType(
				((RosettaBinaryOperation) intOnly.getOperations().get(0).getExpression()).getLeft()).getRType().getName());
	}
}
