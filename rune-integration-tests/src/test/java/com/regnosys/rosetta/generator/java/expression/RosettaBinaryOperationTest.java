package com.regnosys.rosetta.generator.java.expression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.RosettaFunction;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaBinaryOperationTest {

    @Inject
    private CodeGeneratorTestHelper generatorTestHelper;
    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;

    private Map<String, Class<?>> classes;

    @BeforeEach
    void setUp() {
        String code = """
                type NumberList:
                    numbers number (0..*)

                type Foo:
                    bar Bar (0..*)
                    bar2 Bar (0..*)
                    baz Baz (0..1)

                type Bar:
                    before number (0..1)
                    after number (0..1)

                type Baz:
                    bazValue number (0..1)
                    other number (0..1)

                func FeatureCallEqualToLiteral:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before any = 5

                func FeatureCallNotEqualToLiteral:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before all <> 5

                func FeatureCallEqualToFeatureCall:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before = foo -> bar -> after

                func FeatureCallListEqualToFeatureCall:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before any = foo -> baz -> other

                func FeatureCallNotEqualToFeatureCall:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before <> foo -> bar -> after

                func FeatureCallListNotEqualToFeatureCall:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before all <> foo -> baz -> other

                func FeatureCallsEqualToLiteralOr:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before any = 5 or foo -> baz -> other = 5

                func FeatureCallsEqualToLiteralAnd:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar -> before any = 5 and foo -> bar -> after any = 5


                func MultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    alias values : [foo -> bar -> before, foo -> baz -> other]
                    set result:
                        values contains foo -> bar -> after
                            or values contains foo -> baz -> bazValue


                func MultipleAndFeatureCallsEqualToMultipleOrFeatureCalls:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        [foo -> bar -> before,  foo -> baz -> other] = [foo -> bar -> after, foo -> baz -> bazValue]


                func FeatureCallComparisonOr:
                    inputs: foo Foo(1..1)
                    output: result boolean (1..1)
                    set result:
                        (foo -> bar -> before any = foo -> baz -> other) or (foo -> bar -> after any = foo -> baz -> bazValue)


                func FeatureCallComparisonAnd:
                    inputs: foo Foo(1..1)
                    output: result boolean (1..1)
                    set result:
                        (foo -> bar -> before any = foo -> baz -> other) and (foo -> bar -> after any = foo -> baz -> bazValue)


                func MultipleOrFeatureCallEqualToLiteral:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        [foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] contains 5.0


                func MultipleAndFeatureCallEqualToLiteral:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        [foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] any = 5.0


                func AliasFeatureCallEqualToLiteral:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        AliasBefore(foo) -> numbers any = 5


                func AliasFeatureCallEqualToFeatureCall:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        AliasBefore(foo) = AliasAfter(foo)


                func AliasFeatureCallsEqualToLiteralOr:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        AliasBefore(foo) -> numbers any = 5 or  AliasOther(foo) -> numbers any = 5


                func AliasFeatureCallsEqualToLiteralAnd:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        AliasBefore(foo) -> numbers any = 5 and AliasOther(foo) -> numbers any = 5


                func AliasMultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
                    inputs: foo Foo(1..1)
                    output: result boolean (1..1)
                    set result:
                        (AliasBefore(foo) -> numbers exists
                        or AliasOther(foo) -> numbers exists) =
                        (AliasAfter(foo) -> numbers contains foo -> baz -> bazValue)


                func AliasMultipleOrs:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        AliasBeforeOrAfterOrOther(foo) -> numbers contains 5.0


                func MultipleGreaterThanComparisonsWithOrAnd:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar first -> before > 5
                            or ( foo -> baz -> other > 10 and foo -> bar first -> after > 15 )
                            or foo -> baz -> bazValue > 20

                func FeatureCallGreatherThan:
                    inputs: foo Foo (1..1)
                    output: result boolean (1..1)
                    set result:
                        foo -> bar first -> before > foo -> bar2 first -> before


                func AliasBefore:
                    inputs: foo Foo (1..1)
                    output: result NumberList (1..1)
                    set result -> numbers: foo -> bar -> before

                func AliasAfter:
                    inputs: foo Foo (1..1)
                    output: result NumberList (1..1)
                    set result -> numbers : foo -> bar -> after

                func AliasOther:
                    inputs: foo Foo (1..1)
                    output: result NumberList (1..1)
                    set result -> numbers : foo -> baz -> other


                func AliasBeforeOrAfterOrOther:
                    inputs: foo Foo (1..1)
                    output: result NumberList (1..1)
                    set result -> numbers : [
                        foo -> bar -> before,
                        foo -> bar -> after,
                        foo -> baz -> other
                    ]

                """;
        // generatorTestHelper.writeClasses(generatorTestHelper.generateCode(code), "QualifyEventsComparisonTest");
        classes = generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldCompareEqualObjects() {
        Object bar = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(5), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("other", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of("bar", ImmutableList.of(bar))));

        assertResult("FeatureCallEqualToLiteral", foo, true);
        assertResult("FeatureCallEqualToFeatureCall", foo, true);
        assertResult("FeatureCallsEqualToLiteralOr", foo, true);
        assertResult("FeatureCallsEqualToLiteralAnd", foo, true);
        assertResult("FeatureCallNotEqualToLiteral", foo, false);
        assertResult("FeatureCallNotEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListEqualToFeatureCall", foo, true);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, false);
    }

    @Test
    void shouldCompareEqualObjectsWithMultipleCardinality() {
        Object bar1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(5), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object bar2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(5), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("other", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of("bar", ImmutableList.of(bar1, bar2))));

        assertResult("FeatureCallEqualToLiteral", foo, true);
        assertResult("FeatureCallEqualToFeatureCall", foo, true);
        assertResult("FeatureCallsEqualToLiteralOr", foo, true);
        assertResult("FeatureCallsEqualToLiteralAnd", foo, true);
        assertResult("FeatureCallNotEqualToLiteral", foo, false);
        assertResult("FeatureCallNotEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListEqualToFeatureCall", foo, true);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, false);
    }

    @Test
    void shouldCompareObjectsWithZeroCardinality() {
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("other", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of()));

        assertResult("FeatureCallEqualToLiteral", foo, false);
        assertResult("FeatureCallEqualToFeatureCall", foo, true);
        assertResult("FeatureCallNotEqualToLiteral", foo, true);
        assertResult("FeatureCallNotEqualToFeatureCall", foo, true);
        assertResult("FeatureCallListEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, true);
    }

    @Test
    void shouldCompareObjectsWithUnsetValues() {
        Object bar1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(5), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object bar2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(5), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("bazValue", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of("bar", ImmutableList.of(bar1, bar2))));

        assertResult("FeatureCallListEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, true);
    }

    @Test
    void shouldCompareUnequalObjects() {
        Object bar = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(4), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("other", BigDecimal.valueOf(10)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of("bar", ImmutableList.of(bar))));

        assertResult("FeatureCallEqualToLiteral", foo, false);
        assertResult("FeatureCallEqualToFeatureCall", foo, false);
        assertResult("FeatureCallsEqualToLiteralOr", foo, false);
        assertResult("FeatureCallsEqualToLiteralAnd", foo, false);
        assertResult("FeatureCallNotEqualToLiteral", foo, true);
        assertResult("FeatureCallListEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, true);
    }

    @Test
    void shouldCompareUnequalObjectsWithMultipleCardinality() {
        Object bar1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(4), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object bar2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar",
                ImmutableMap.<String, Object>of("before", BigDecimal.valueOf(4), "after", BigDecimal.valueOf(5)), ImmutableMap.<String, List<?>>of());
        Object baz = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz",
                ImmutableMap.<String, Object>of("other", BigDecimal.valueOf(10)), ImmutableMap.<String, List<?>>of());
        RosettaModelObject foo = (RosettaModelObject) (generatorTestHelper.createInstanceUsingBuilder(classes, "Foo",
                ImmutableMap.<String, Object>of("baz", baz), ImmutableMap.<String, List<?>>of("bar", ImmutableList.of(bar1, bar2))));

        assertResult("FeatureCallEqualToLiteral", foo, false);
        assertResult("FeatureCallEqualToFeatureCall", foo, false);
        assertResult("FeatureCallsEqualToLiteralOr", foo, false);
        assertResult("FeatureCallsEqualToLiteralAnd", foo, false);
        assertResult("FeatureCallNotEqualToLiteral", foo, true);
        assertResult("FeatureCallListEqualToFeatureCall", foo, false);
        assertResult("FeatureCallListNotEqualToFeatureCall", foo, true);
    }

    @Test
    void shouldGenerateBooleanOrComparisonResult() {
        String model = """
                func FuncFoo:
                    inputs:
                        foo Foo (1..1)
                    output:
                        result boolean (1..1)

                    set result:
                        foo -> attrBoolean or foo -> attrNumber = 5

                type Foo:
                    attrBoolean boolean (1..1)
                    attrNumber number (1..1)
                """;
        Map<String, String> code = generatorTestHelper.generateCode(model);
        String funcFoo = code.get("com.rosetta.test.model.functions.FuncFoo");
        // Built line-by-line (rather than a text block) to preserve the exact trailing
        // whitespace the generator emits on the javadoc and blank-body lines.
        String expected = String.join("\n",
                "package com.rosetta.test.model.functions;",
                "",
                "import com.google.inject.ImplementedBy;",
                "import com.rosetta.model.lib.expression.CardinalityOperator;",
                "import com.rosetta.model.lib.expression.ComparisonResult;",
                "import com.rosetta.model.lib.functions.RosettaFunction;",
                "import com.rosetta.model.lib.mapper.MapperS;",
                "import com.rosetta.test.model.Foo;",
                "import java.math.BigDecimal;",
                "",
                "import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;",
                "",
                "@ImplementedBy(FuncFoo.FuncFooDefault.class)",
                "public abstract class FuncFoo implements RosettaFunction {",
                "",
                "\t/**",
                "\t* @param foo ",
                "\t* @return result ",
                "\t*/",
                "\tpublic Boolean evaluate(Foo foo) {",
                "\t\tBoolean result = doEvaluate(foo);",
                "\t\t",
                "\t\treturn result;",
                "\t}",
                "",
                "\tprotected abstract Boolean doEvaluate(Foo foo);",
                "",
                "\tpublic static class FuncFooDefault extends FuncFoo {",
                "\t\t@Override",
                "\t\tprotected Boolean doEvaluate(Foo foo) {",
                "\t\t\tBoolean result = null;",
                "\t\t\treturn assignOutput(result, foo);",
                "\t\t}",
                "\t\t",
                "\t\tprotected Boolean assignOutput(Boolean result, Foo foo) {",
                "\t\t\tresult = ComparisonResult.ofNullSafe(MapperS.of(foo).<Boolean>map(\"getAttrBoolean\", _foo -> _foo.getAttrBoolean())).orNullSafe(areEqual(MapperS.of(foo).<BigDecimal>map(\"getAttrNumber\", _foo -> _foo.getAttrNumber()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All)).get();",
                "\t\t\t",
                "\t\t\treturn result;",
                "\t\t}",
                "\t}",
                "}",
                "");
        // Normalize line endings so the comparison holds on Windows, where the generator
        // emits '\r\n' but the expected value is built with '\n'.
        assertEquals(expected, funcFoo.replace("\r\n", "\n"));
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateBooleanAndComparisonResult() {
        String model = """
                func FuncFoo:
                    inputs:
                        foo Foo (1..1)
                    output:
                        result boolean (1..1)

                    set result:
                        foo -> attrBoolean and foo -> attrNumber = 5

                type Foo:
                    attrBoolean boolean (1..1)
                    attrNumber number (1..1)
                """;
        Map<String, String> code = generatorTestHelper.generateCode(model);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateBooleanAndComparisonResult2() {
        String model = """
                func FuncFoo:
                    inputs:
                        foo Foo (1..1)
                    output:
                        result boolean (1..1)

                    set result:
                        foo -> attrNumber = 5 and foo -> attrBoolean

                type Foo:
                    attrBoolean boolean (1..1)
                    attrNumber number (1..1)
                """;
        Map<String, String> code = generatorTestHelper.generateCode(model);
        generatorTestHelper.compileToClasses(code);
    }

    // Util methods

    private void assertResult(String funcName, RosettaModelObject input, boolean expectedResult) {
        RosettaFunction func = functionGeneratorHelper.createFunc(classes, funcName);
        Boolean res = functionGeneratorHelper.invokeFunc(func, Boolean.class, input);
        assertThat(res, is(expectedResult));
    }
}
