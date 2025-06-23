package com.regnosys.rosetta.types;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

// Should be moved over to Java RosettaTypeProviderTest
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RosettaTypeProviderXtendTest {

    @Inject
    @Extension
    private RosettaTypeProvider _rosettaTypeProvider;

    @Inject
    @Extension
    private CardinalityProvider _cardinalityProvider;

    @Inject
    @Extension
    private ModelHelper modelHelper;

    @Inject
    @Extension
    private ValidationTestHelper _validationTestHelper;

    @Inject
    @Extension
    private ExpressionParser _expressionParser;

    @Inject
    @Extension
    private TypeFactory _typeFactory;

    @Inject
    @Extension
    private RObjectFactory _rObjectFactory;

    @Inject
    @Extension
    private TypeSystem _typeSystem;

    @Inject
    @Extension
    private RBuiltinTypeService builtins;

    private RMetaAttribute SCHEME;

    @BeforeAll
    public void setup() {
        SCHEME = new RMetaAttribute("scheme", this.builtins.UNCONSTRAINED_STRING);
    }

    private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, List<RosettaModel> context, String... attributes) {
        assertIsValidWithType(this._expressionParser.parseExpression(expr, context, ((Collection<? extends CharSequence>)Conversions.doWrapArray(attributes))), expr, expectedType, expectedIsMulti);
    }

    private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, List<RosettaModel> context) {
        assertIsValidWithType(this._expressionParser.parseExpression(expr, context), expr, expectedType, expectedIsMulti);
    }

    private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, String... attributes) {
        assertIsValidWithType(this._expressionParser.parseExpression(expr, ((Collection<? extends CharSequence>)Conversions.doWrapArray(attributes))), expr, expectedType, expectedIsMulti);
    }

    private void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
        assertIsValidWithType(this._expressionParser.parseExpression(expr), expr, expectedType, expectedIsMulti);
    }

    private void assertIsValidWithType(RosettaExpression expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
        assertIsValidWithType(expr, NodeModelUtils.findActualNodeFor(expr).getText(), expectedType, expectedIsMulti);
    }

    private void assertIsValidWithType(RosettaExpression expr, CharSequence originalExpression, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
        this._validationTestHelper.assertNoIssues(expr);
        RMetaAnnotatedType actual = this._rosettaTypeProvider.getRMetaAnnotatedType(expr);

        Assertions.assertEquals(expectedType, actual, "Expression: " + originalExpression);
        if (expectedIsMulti) {
            Assertions.assertTrue(this._cardinalityProvider.isMulti(expr), "Expected multi cardinality. Expression: " + originalExpression);
        } else {
            Assertions.assertFalse(this._cardinalityProvider.isMulti(expr), "Expected single cardinality. Expression: " + originalExpression);
        }
    }

    @Test
    public void testLiteralTypeInference() {
        assertIsValidWithType("False", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("\"Some string\"", this._typeFactory.stringWithNoMeta(11, 11), false);
        assertIsValidWithType("3.14", this._typeFactory.numberWithNoMeta(3, 2, "3.14", "3.14"), false);
        assertIsValidWithType("1", this._typeFactory.intWithNoMeta(1, "1", "1"), false);
        assertIsValidWithType("empty", this.builtins.NOTHING_WITH_ANY_META, false);
    }

    @Test
    public void testVariableTypeInference() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        func TestVar:
            output: result number (1..4)
            alias c: if True then 42 else -1/12
            add result:
                c

        func TestImplicitVar:
            output: result int (3..3)
            add result:
                [1, 2, 3] extract item + 1
        """));
        assertIsValidWithType("a", RMetaAnnotatedType.withMeta(this.builtins.UNCONSTRAINED_INT, Collections.<RMetaAttribute>unmodifiableList(CollectionLiterals.<RMetaAttribute>newArrayList(this.SCHEME))), true, new String[]{"a int (2..4) [metadata scheme]"});
        assertIsValidWithType("b", this.builtins.BOOLEAN_WITH_NO_META, false, new String[]{"b boolean (1..1)"});
        Function function = (Function) context.get(0).getElements().get(0);
        assertIsValidWithType(function.getOperations().get(0).getExpression(), this.builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, false);
    }

    @Test
    public void testLogicalOperationTypeInference() {
        assertIsValidWithType("True and False", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("True or False", this.builtins.BOOLEAN_WITH_NO_META, false);
    }

    @Test
    public void testLogicalOperationTypeChecking() {
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("1 or False"), LOGICAL_OPERATION, null, "Expected type `boolean`, but got `int` instead. Cannot use `int` with operator `or`");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("True or 3.14"), LOGICAL_OPERATION, null, "Expected type `boolean`, but got `number` instead. Cannot use `number` with operator `or`");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("a or False", Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList("a boolean (1..2)"))), LOGICAL_OPERATION, null, "Expecting single cardinality. The `or` operator requires a single cardinality input");
    }

    @Test
    public void testEqualityOperationTypeInference() {
        assertIsValidWithType("1 = 2", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("1 <> 2", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("\"a\" = \"b\"", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("\"a\" <> \"b\"", this.builtins.BOOLEAN_WITH_NO_META, false);
    }

    @Test
    public void testEqualityOperationTypeChecking() {
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("1 = True"), EQUALITY_OPERATION, null, "Types `int` and `boolean` are not comparable");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("[1, 2] <> [True, False, False]"), EQUALITY_OPERATION, null, "Types `int` and `boolean` are not comparable");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("1 = True"), EQUALITY_OPERATION, null, "Types `int` and `boolean` are not comparable");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 3] any <> a", Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList("a int (1..2)"))), EQUALITY_OPERATION, null, "Expecting single cardinality");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 2] all = [1, 2]"), EQUALITY_OPERATION, null, "Expecting single cardinality");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("5 any <> [1, 2]"), EQUALITY_OPERATION, null, "Expecting multi cardinality. Did you mean to flip around the operands of the `<>` operator?");
    }

    @Test
    public void testArithmeticOperationTypeInference() {
        assertIsValidWithType("3 + 4", this._typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(7)), Optional.of(BigInteger.valueOf(7))), false);
        assertIsValidWithType("3.0 + 4", this._typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
        assertIsValidWithType("3 + 4.0", this._typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
        assertIsValidWithType("3.0 + 4.0", this._typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty()), false);
        assertIsValidWithType("\"ab\" + \"cd\"", this._typeFactory.stringWithNoMeta(4, 4), false);

        assertIsValidWithType("3 - 4", this._typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(-1)), Optional.of(BigInteger.valueOf(-1))), false);
        assertIsValidWithType("3 - 4.0", this._typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1), Optional.of(new BigDecimal("-1")), Optional.of(new BigDecimal("-1")), Optional.empty()), false);

        assertIsValidWithType("3 * 4", this._typeFactory.intWithNoMeta(Optional.empty(), Optional.of(BigInteger.valueOf(12)), Optional.of(BigInteger.valueOf(12))), false);
        assertIsValidWithType("3.0 * 4", this._typeFactory.numberWithNoMeta(Optional.empty(), Optional.of(1), Optional.of(new BigDecimal("12")), Optional.of(new BigDecimal("12")), Optional.empty()), false);

        assertIsValidWithType("3 / 4", this.builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, false);
    }

    @Test
    public void testArithmeticOperationTypeChecking() {
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 2] + 3"), ARITHMETIC_OPERATION, null, "Expecting single cardinality. The `+` operator requires a single cardinality input");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("1.5 * False"), ARITHMETIC_OPERATION, null, "Expected type `number`, but got `boolean` instead. Cannot use `boolean` with operator `*`");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("\"ab\" + 3"), ARITHMETIC_OPERATION, null, "Expected type `string`, but got `int` instead. Cannot add `int` to a `string`");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("a + 5", Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList("a int (1..2)"))), ARITHMETIC_OPERATION, null, "Expecting single cardinality. The `+` operator requires a single cardinality input");
    }

    @Test
    public void testComparisonOperationTypeInference() {
        assertIsValidWithType("1 < 2", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("3 > 3.14", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("-5.1 <= 42", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("-3.14 >= 3.14", this.builtins.BOOLEAN_WITH_NO_META, false);

        assertIsValidWithType("[1, 2] any < 5", this.builtins.BOOLEAN_WITH_NO_META, false);
    }

    @Test
    public void testComparisonOperationTypeChecking() {
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 2] < 3"), COMPARISON_OPERATION, null, "Expecting single cardinality. Did you mean to use `all` or `any` in front of the `<` operator?");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("1.5 <= False"), COMPARISON_OPERATION, null, "Expected type `number`, but got `boolean` instead. Cannot compare a `boolean` to a `number`");

        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("a < 5", Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList("a int (1..2)"))), COMPARISON_OPERATION, null, "Expecting single cardinality. Did you mean to use `all` or `any` in front of the `<` operator?");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 2] any < a", Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList("a int (1..2)"))), COMPARISON_OPERATION, null, "Expecting single cardinality");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("empty any < empty"), COMPARISON_OPERATION, null, "Expecting multi cardinality. Did you mean to remove the `any` modifier on the `<` operator?");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("[1, 2] all > [1, 2]"), COMPARISON_OPERATION, null, "Expecting single cardinality");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("5 any <= [1, 2]"), COMPARISON_OPERATION, null, "Expecting single cardinality. Did you mean to flip around the operands of the `<=` operator?");
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("5 all >= 1"), COMPARISON_OPERATION, null, "Expecting multi cardinality. Did you mean to remove the `all` modifier on the `>=` operator?");
    }

    @Test
    public void testConditionalExpressionTypeInference() {
        assertIsValidWithType("if True then [1, 2] else [3.0, 4.0, 5.0, 6.0]", this._typeFactory.numberWithNoMeta(2, 1, "1", "6"), true);
    }

    @Test
    public void testConditionalExpressionTypeChecking() {
        this._validationTestHelper.assertWarning(this._expressionParser.parseExpression("if [True, False] then 1 else 2"), ROSETTA_CONDITIONAL_EXPRESSION, null, "Expecting single cardinality. The condition of an if-then-else expression should be single cardinality");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("if True then 1 else False"), ROSETTA_CONDITIONAL_EXPRESSION, null, "Types `int` and `boolean` do not have a common supertype");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("if True then [1, 2, 3] else [False, True]"), ROSETTA_CONDITIONAL_EXPRESSION, null, "Types `int` and `boolean` do not have a common supertype");
    }

    @Test
    public void testListLiteralTypeInference() {
        assertIsValidWithType("[]", this.builtins.NOTHING_WITH_ANY_META, false);
        assertIsValidWithType("[2, 4.5, 7, -3.14]", this._typeFactory.numberWithNoMeta(3, 2, "-3.14", "7"), true);
        assertIsValidWithType("[2, [1, 2], -3.14]", this._typeFactory.numberWithNoMeta(3, 2, "-3.14", "2"), true);
    }

    @Test
    public void testListLiteralTypeChecking() {
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("[1, True]"), LIST_LITERAL, null, "Types `int` and `boolean` do not have a common supertype");
    }

    @Test
    public void testFunctionCallTypeInference() {
        RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
        func SomeFunc:
            inputs:
                a int (1..1)
                b boolean (2..4)
            output: result number (3..5)
            add result:
                [1.0, 2.0, 3.0]
        """);
        assertIsValidWithType("SomeFunc(42, [True, False, True])", this.builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, true, Collections.singletonList(context));
    }

    @Test
    public void testFunctionCallTypeChecking() {
        RosettaModel context = modelHelper.parseRosettaWithNoIssues("""
        namespace test

        func SomeFunc:
            inputs:
                a int (1..1)
                b boolean (2..4)
            output: result int (1..1)
            set result:
                42
        """);

        this._validationTestHelper.assertError(this._expressionParser.parseExpression("SomeFunc(1, [False, True], True)", Collections.singletonList(context)), ROSETTA_SYMBOL_REFERENCE, null, "Expected 2 arguments, but got 3 instead");
        this._validationTestHelper.assertError(this._expressionParser.parseExpression("SomeFunc(1, [2, 3])", Collections.singletonList(context)), ROSETTA_SYMBOL_REFERENCE, null, "Expected type `boolean`, but got `int` instead. Cannot assign `int` to input `b`");
    }

    @Test
    public void testProjectionTypeInference() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        type TestType:
            a number (1..1)
            b string (0..*)
        """));

        assertIsValidWithType("t -> a", this.builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, false, context, "t TestType (1..1)");
        assertIsValidWithType("t -> b", this.builtins.UNCONSTRAINED_STRING_WITH_NO_META, true, context, "t TestType (1..1)");
    }

    @Test
    public void testEnumTypeInference() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        enum TestEnum:
            A
            B
            C
        """));

        RosettaRootElement element = context.get(0).getElements().get(0);
        REnumType enumType = this._rObjectFactory.buildREnumType((RosettaEnumeration) element);
        assertIsValidWithType("TestEnum -> A", RMetaAnnotatedType.withNoMeta(enumType), false, context);
    }

    @Test
    public void testExistsTypeInference() {
        assertIsValidWithType("a exists", this.builtins.BOOLEAN_WITH_NO_META, false, new String[]{"a int (0..1)"});
        assertIsValidWithType("a exists", this.builtins.BOOLEAN_WITH_NO_META, false, new String[]{"a int (0..3)"});
    }

    @Test
    public void testExistsTypeChecking() {
        // Empty test in the generated code
    }

    @Test
    public void testAbsentTypeInference() {
        assertIsValidWithType("a is absent", this.builtins.BOOLEAN_WITH_NO_META, false, new String[]{"a int (0..1)"});
        assertIsValidWithType("a is absent", this.builtins.BOOLEAN_WITH_NO_META, false, new String[]{"a int (0..3)"});
    }

    @Test
    public void testAbsentTypeChecking() {
        // Empty test in the generated code
    }

    @Test
    public void testCountTypeInference() {
        RMetaAnnotatedType positiveInt = this._typeFactory.intWithNoMeta(Optional.<Integer>empty(), Optional.<BigInteger>of(BigInteger.ZERO), Optional.<BigInteger>empty());
        assertIsValidWithType("empty count", positiveInt, false);
        assertIsValidWithType("42 count", positiveInt, false);
        assertIsValidWithType("[1, 2, 3] count", positiveInt, false);
        assertIsValidWithType("(if True then empty else [1, 2, 3]) count", positiveInt, false);
    }

    @Test
    public void testOnlyExistsTypeInference() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        namespace test

        type A:
            x int (0..1)
            y number (0..3)
            z boolean (0..*)

            condition C:
                x only exists and (x, y) only exists
        """));

        assertIsValidWithType("a -> x only exists", this.builtins.BOOLEAN_WITH_NO_META, false, context, "a A (1..1)");
        assertIsValidWithType("(a -> x, a -> y) only exists", this.builtins.BOOLEAN_WITH_NO_META, false, context, "a A (1..1)");
    }

    @Test
    public void testOnlyExistsTypeChecking() {
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

        Data data = (Data) model.getElements().get(0);
        this._validationTestHelper.assertError(data.getConditions().get(0).getExpression(), ROSETTA_SYMBOL_REFERENCE, null, "Operator `only exists` is not supported for type `Foo`. All attributes of input type should be optional");

        EList<Condition> conditions = ((Data) model.getElements().get(1)).getConditions();
        LogicalOperation logicalOperation1 = (LogicalOperation) conditions.get(0).getExpression();
        this._validationTestHelper.assertError(logicalOperation1.getLeft(), ROSETTA_SYMBOL_REFERENCE, null, "All parent paths must be equal");
        this._validationTestHelper.assertError(logicalOperation1.getRight(), ROSETTA_SYMBOL_REFERENCE, null, "All parent paths must be equal");

        LogicalOperation logicalOperation2 = (LogicalOperation) conditions.get(1).getExpression();
        this._validationTestHelper.assertError(logicalOperation2.getLeft(), ROSETTA_ONLY_EXISTS_EXPRESSION, null, "Duplicate attribute");
        this._validationTestHelper.assertError(logicalOperation2.getRight(), ROSETTA_ONLY_EXISTS_EXPRESSION, null, "Duplicate attribute");

        EList<Operation> operations = ((Function) model.getElements().get(2)).getOperations();
        this._validationTestHelper.assertWarning(operations.get(0).getExpression(), ROSETTA_SYMBOL_REFERENCE, null, "Expecting single cardinality. The `only exists` operator requires a single cardinality input");
        this._validationTestHelper.assertError(operations.get(1).getExpression(), ROSETTA_ONLY_EXISTS_EXPRESSION, null, "Object must have a parent object");
        this._validationTestHelper.assertError(operations.get(2).getExpression(), ROSETTA_SYMBOL_REFERENCE, null, "All parent paths must be equal");
        this._validationTestHelper.assertError(operations.get(3).getExpression(), ROSETTA_SYMBOL_REFERENCE, null, "Operator `only exists` is not supported for type `Foo`. All attributes of input type should be optional");
    }

    @Test
    public void testOnlyElementTypeInference() {
        assertIsValidWithType("(if True then 0 else [1, 2]) only-element", this._typeFactory.intWithNoMeta(1, "0", "2"), false);
        assertIsValidWithType("(if True then empty else [True, False]) only-element", this.builtins.BOOLEAN_WITH_NO_META, false);
        assertIsValidWithType("(if True then 0 else [1, 2, 3, 42.0]) only-element", this._typeFactory.numberWithNoMeta(3, 1, "0", "42.0"), false);
    }

    @Test
    public void testOnlyElementTypeChecking() {
        // Empty test in the generated code
    }

    @Test
    public void testTypeAliasJoin() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        namespace test

        type A:
            x string (1..1) [metadata scheme]
            y string (1..1) [metadata scheme]
        """));

        assertIsValidWithType("a -> x", RMetaAnnotatedType.withMeta(this.builtins.UNCONSTRAINED_STRING, Collections.<RMetaAttribute>unmodifiableList(CollectionLiterals.<RMetaAttribute>newArrayList(this.SCHEME))), false, context, "a A (1..1)");
        assertIsValidWithType("a -> y", RMetaAnnotatedType.withMeta(this.builtins.UNCONSTRAINED_STRING, Collections.<RMetaAttribute>unmodifiableList(CollectionLiterals.<RMetaAttribute>newArrayList(this.SCHEME))), false, context, "a A (1..1)");
    }

    @Test
    public void shouldCoerceStringToParameterizedString() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        func Test:
            inputs: str string (1..1)
            output: max3String string(minLength: 1, maxLength: 3) (1..1)
            set max3String: str
        """);
    }

    @Test
    public void shouldCoerceParameterizedStringToString() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        func Test:
            inputs: max3String string(minLength: 1, maxLength: 3) (1..1)
            output: str string (1..1)
            set str: max3String
        """);
    }

    @Test
    public void shouldCoerceStringToStringTypeAlias() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        typeAlias Max3String: string(minLength: 1, maxLength: 3)

        func Test:
            inputs: str string (1..1)
            output: max3String Max3String (1..1)
            set max3String: str
        """);
    }

    @Test
    public void shouldCoerceStringTypeAliasToString() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        typeAlias Max3String: string(minLength: 1, maxLength: 3)

        func Test:
            inputs: max3String Max3String (1..1)
            output: str string (1..1)
            set str: max3String
        """);
    }

    @Test
    public void shouldCoerceDifferentParameterizedStrings() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        func Test:
            inputs: max10String string(minLength: 1, maxLength: 10) (1..1)
            output: max3String string(minLength: 1, maxLength: 3) (1..1)
            set max3String: max10String
        """);
    }

    @Test
    public void shouldCoerceDifferentTypeAliases() {
        modelHelper.parseRosettaWithNoIssues("""
        namespace test

        typeAlias Max10String: string(minLength: 1, maxLength: 10)
        typeAlias Max3String: string(minLength: 1, maxLength: 3)

        func Test:
            inputs: max10String Max10String (1..1)
            output: max3String Max3String (1..1)
            set max3String: max10String
        """);
    }

    @Test
    public void testAttributeSameNameAsAnnotationTest() {
        List<RosettaModel> context = Collections.singletonList(modelHelper.parseRosettaWithNoIssues("""
        namespace test

        type TestType:
            scheme string (1..1)
        """));

        assertIsValidWithType("t -> scheme", this.builtins.UNCONSTRAINED_STRING_WITH_NO_META, false, context, "t TestType (1..1)");
    }

}
