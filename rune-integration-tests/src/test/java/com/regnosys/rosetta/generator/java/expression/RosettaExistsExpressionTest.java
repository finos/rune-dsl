package com.regnosys.rosetta.generator.java.expression;

import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExistsExpressionTest {

    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;
    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;

    private Map<String, Class<?>> classes;

    @BeforeEach
    public void setUp() {
        String code = """
            type Foo:
                bar Bar (0..*)
                baz Baz (0..1)
            
            type Bar:
                before number (0..1)
                after number (0..1)
                other number (0..1)
                beforeWithScheme number (0..1)
                    [metadata scheme]
                afterWithScheme number (0..1)
                    [metadata scheme]
                beforeList number (0..*)
                afterList number (0..*)
                beforeListWithScheme number (0..*)
                    [metadata scheme]
                afterListWithScheme number (0..*)
                    [metadata scheme]
            
            type Baz:
                bazValue number (0..1)
                other number (0..1)
            
            func Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists
            
            func SingleExists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before single exists

            func MultipleExists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before multiple exists

            func OnlyExists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract before only exists
            
            func OnlyExistsMultiplePaths:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( before, after ) only exists

            func OnlyExistsPathWithScheme:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( before, afterWithScheme ) only exists
            
            func OnlyExistsBothPathsWithScheme:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( beforeWithScheme, afterWithScheme ) only exists

            func OnlyExistsListMultiplePaths:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( before, afterList ) only exists

            func OnlyExistsListPathWithScheme:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( before, afterListWithScheme ) only exists
            
            func OnlyExistsListBothPathsWithScheme:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar first then extract ( beforeListWithScheme, afterListWithScheme ) only exists

            func MultipleSeparateOr_NoAliases_Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists or foo -> bar -> after exists

            func MultipleOr_NoAliases_Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists or foo -> bar -> after exists or foo -> baz -> other exists
            
            func MultipleOrBranchNode_NoAliases_Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar exists or foo -> baz exists
            
            func MultipleAnd_NoAliases_Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists and foo -> bar -> after exists and foo -> baz -> other exists
            
            func MultipleOrAnd_NoAliases_Exists:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists or ( foo -> bar -> after exists and foo -> baz -> other exists )
            
            func MultipleOrAnd_NoAliases_Exists2:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    (foo -> bar -> before exists and foo -> bar -> after exists) or foo -> baz -> other exists or foo -> baz -> bazValue exists
            
            func MultipleOrAnd_NoAliases_Exists3:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    (foo -> bar -> before exists or foo -> bar -> after exists) or (foo -> baz -> other exists and foo -> baz -> bazValue exists)
            
            func MultipleExistsWithOrAnd:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> before exists or ( foo -> baz -> other exists and foo -> bar -> after exists ) or foo -> baz -> bazValue exists
            """;

        var generated = codeGeneratorTestHelper.generateCode(code);
        classes = codeGeneratorTestHelper.compileToClasses(generated);
    }

    @Test
    public void shouldGenerateFuncWithExistsAndSingleExists() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("before", BigDecimal.valueOf(15)),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("Exists", foo, true);
        assertResult("SingleExists", foo, true);
    }

    @Test
    public void shouldGenerateFuncWithOnlyExists1() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("before", BigDecimal.valueOf(15)),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExists", foo, true);
        assertResult("OnlyExistsMultiplePaths", foo, false);
        assertResult("OnlyExistsPathWithScheme", foo, false);
        assertResult("OnlyExistsBothPathsWithScheme", foo, false);
    }

    @Test
    public void shouldGenerateFuncWithOnlyExists2() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of(
                "before", BigDecimal.valueOf(15),
                "after", BigDecimal.valueOf(20)
            ),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExists", foo, false);
        assertResult("OnlyExistsMultiplePaths", foo, true);
        assertResult("OnlyExistsPathWithScheme", foo, false);
        assertResult("OnlyExistsBothPathsWithScheme", foo, false);
    }

    @Test
    public void shouldGenerateFuncWithOnlyExists3() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of(
                "before", BigDecimal.valueOf(15),
                "afterWithSchemeValue", BigDecimal.valueOf(20)
            ),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExists", foo, false);
        assertResult("OnlyExistsMultiplePaths", foo, false);
        assertResult("OnlyExistsPathWithScheme", foo, true);
        assertResult("OnlyExistsBothPathsWithScheme", foo, false);
    }

    @Test
    public void shouldGenerateFuncWithOnlyExists4() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of(
                "beforeWithSchemeValue", BigDecimal.valueOf(15),
                "afterWithSchemeValue", BigDecimal.valueOf(20)
            ),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExists", foo, false);
        assertResult("OnlyExistsMultiplePaths", foo, false);
        assertResult("OnlyExistsPathWithScheme", foo, false);
        assertResult("OnlyExistsBothPathsWithScheme", foo, true);
    }

    @Test
    public void shouldGenerateFuncWithOnlyExists5() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("before", BigDecimal.valueOf(15)),
            ImmutableMap.of("afterList", Arrays.asList(BigDecimal.valueOf(20), BigDecimal.valueOf(21)))
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExistsListMultiplePaths", foo, true);
        assertResult("OnlyExistsListPathWithScheme", foo, false);
        assertResult("OnlyExistsListBothPathsWithScheme", foo, false);
    }

    @Test
    public void shouldNotQualifyOnlyExists() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of(
                "before", BigDecimal.valueOf(15),
                "after", BigDecimal.valueOf(20),
                "other", BigDecimal.valueOf(25)
            ),
            ImmutableMap.of()
        );
        RosettaModelObject foo = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of(),
                ImmutableMap.of("bar", List.of(bar))
        );

        assertResult("OnlyExists", foo, false);
        assertResult("OnlyExistsMultiplePaths", foo, false);
    }

    private void assertResult(String funcName, RosettaModelObject input, boolean expectedResult) {
        var func = functionGeneratorHelper.createFunc(classes, funcName);
        Boolean res = functionGeneratorHelper.invokeFunc(func, Boolean.class, input);
        assertThat(res, is(expectedResult));
    }
}