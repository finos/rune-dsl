package com.regnosys.rosetta.generator.java.expression;

import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import java.math.BigDecimal;
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
public class RosettaCountOperationTest {

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
                corge number (0..1)
            
            type Bar:
                baz Baz (0..1)
                qux number (0..1)

            type Baz:
                quux number (0..1)

            func BranchNodeCountComparisonToLiteral:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> baz count = 2
            
            func BranchAndLeafNodeCountComparisonToLiterals:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> baz count = 1
                    and foo -> bar -> qux count = 1
            
            func LeafNodeCountComparisonToLiteral:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> qux count = 2
            
            func BranchNodeCountComparisonToFeatureCall:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> baz count = foo -> corge
            
            func LeafNodeCountComparisonToFeatureCall:
                inputs: foo Foo (1..1)
                output: result boolean (1..1)
                set result:
                    foo -> bar -> qux count = foo -> corge
            """;

        var generated = codeGeneratorTestHelper.generateCode(code);
        classes = codeGeneratorTestHelper.compileToClasses(generated);
    }

    @Test
    public void should_match_BranchNodeCountComparisonToLiteral_only() {
        Object baz = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Baz",
            ImmutableMap.of("quux", BigDecimal.valueOf(1.1)),
            ImmutableMap.of()
        );
        Object bar1 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("baz", baz),
            ImmutableMap.of()
        );
        Object bar2 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("baz", baz),
            ImmutableMap.of()
        );
        RosettaModelObject foo = createFoo(List.of(bar1, bar2), 5);

        assertResult("BranchNodeCountComparisonToLiteral", foo, true);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, false);
        assertResult("LeafNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, false);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, false);
    }

    @Test
    public void should_match_BranchAndLeafNodeCountComparisonToLiterals_only() {
        Object baz = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Baz",
            ImmutableMap.of("quux", BigDecimal.valueOf(1.1)),
            ImmutableMap.of()
        );
        Object bar1 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("baz", baz),
            ImmutableMap.of()
        );
        Object bar2 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("qux", BigDecimal.valueOf(1.2)),
            ImmutableMap.of()
        );
        RosettaModelObject foo = createFoo(List.of(bar1, bar2), 5);

        assertResult("BranchNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, true);
        assertResult("LeafNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, false);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, false);
    }

    @Test
    public void should_match_LeafNodeCountComparisonToLiteral_only() {
        Object bar1 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("qux", BigDecimal.valueOf(1.1)),
            ImmutableMap.of()
        );
        Object bar2 = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("qux", BigDecimal.valueOf(1.2)),
            ImmutableMap.of()
        );
        RosettaModelObject foo = createFoo(List.of(bar1, bar2), 5);

        assertResult("BranchNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, false);
        assertResult("LeafNodeCountComparisonToLiteral", foo, true);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, false);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, false);
    }

    @Test
    public void should_match_BranchNodeCountComparisonToFeatureCall_only() {
        Object baz = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Baz",
            ImmutableMap.of("quux", BigDecimal.valueOf(100)),
            ImmutableMap.of()
        );
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("baz", baz),
            ImmutableMap.of()
        );
        RosettaModelObject foo = createFoo(List.of(bar), 1);

        assertResult("BranchNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, false);
        assertResult("LeafNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, true);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, false);
    }

    @Test
    public void should_match_LeafNodeCountComparisonToFeatureCall_only() {
        Object bar = codeGeneratorTestHelper.createInstanceUsingBuilder(
            classes,
            "Bar",
            ImmutableMap.of("qux", BigDecimal.valueOf(1.1)),
            ImmutableMap.of()
        );
        RosettaModelObject foo = createFoo(List.of(bar), 1);

        assertResult("BranchNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, false);
        assertResult("LeafNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, false);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, true);
    }

    @Test
    public void should_notMatchUnsetObject() {
        RosettaModelObject foo = createFoo(List.of(), 5);

        assertResult("BranchNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchAndLeafNodeCountComparisonToLiterals", foo, false);
        assertResult("LeafNodeCountComparisonToLiteral", foo, false);
        assertResult("BranchNodeCountComparisonToFeatureCall", foo, false);
        assertResult("LeafNodeCountComparisonToFeatureCall", foo, false);
    }

    private void assertResult(String funcName, RosettaModelObject input, boolean expectedResult) {
        var func = functionGeneratorHelper.createFunc(classes, funcName);
        Boolean res = functionGeneratorHelper.invokeFunc(func, Boolean.class, input);
        assertThat(res, is(expectedResult));
    }

    private RosettaModelObject createFoo(List<Object> bars, int corge) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of("corge", BigDecimal.valueOf(corge)),
                ImmutableMap.of("bar", bars)
        );
    }
}