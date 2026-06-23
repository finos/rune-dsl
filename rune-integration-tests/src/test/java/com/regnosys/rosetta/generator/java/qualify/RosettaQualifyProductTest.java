package com.regnosys.rosetta.generator.java.qualify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.qualify.QualifyResult;
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
public class RosettaQualifyProductTest {

    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;
    @Inject
    private QualifyTestHelper qualifyTestHelper;

    private Map<String, Class<?>> classes;

    @BeforeEach
    public void setUp() {
        String code = """
            isProduct root Foo;

            type Foo:
                bar Bar (0..*)
                corge number (0..1)

            type Bar:
                baz Baz (0..1)
                qux number (0..1)

            type Baz:
                quux number (0..1)

            func Qualify_BranchNodeCountComparisonToLiteral:
                [qualification Product]
                inputs: foo Foo (1..1)
                output: is_product boolean (1..1)
                set is_product:
                    foo -> bar -> baz count = 2
            """;
        var generated = codeGeneratorTestHelper.generateCode(code);
        classes = codeGeneratorTestHelper.compileToClasses(generated);
    }

    @Test
    public void should_match_BranchNodeCountComparisonToLiteral_only() {
        Object baz = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Baz", ImmutableMap.of("quux", BigDecimal.valueOf(1.1)), ImmutableMap.of());
        Object bar1 = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Bar", ImmutableMap.of("baz", baz), ImmutableMap.of());
        Object bar2 = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Bar", ImmutableMap.of("baz", baz), ImmutableMap.of());
        RosettaModelObject foo = createFoo(ImmutableList.of(bar1, bar2), 5);

        List<QualifyResult> results = qualifyTestHelper.createUtilAndGetAllResults(foo);

        assertResult(results, "BranchNodeCountComparisonToLiteral", true);
    }

    @Test
    public void should_match_BranchAndLeafNodeCountComparisonToLiterals_only() {
        Object baz = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Baz", ImmutableMap.of("quux", BigDecimal.valueOf(1.1)), ImmutableMap.of());
        Object bar1 = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Bar", ImmutableMap.of("baz", baz), ImmutableMap.of());
        Object bar2 = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Bar", ImmutableMap.of("qux", BigDecimal.valueOf(1.2)), ImmutableMap.of());
        RosettaModelObject foo = createFoo(ImmutableList.of(bar1, bar2), 5);

        List<QualifyResult> results = qualifyTestHelper.createUtilAndGetAllResults(foo);

        assertResult(results, "BranchNodeCountComparisonToLiteral", false);
    }

    private void assertResult(List<QualifyResult> results, String isProductName, boolean expectedSuccess) {
        QualifyResult result = results.stream()
                .filter(r -> r.getName().equals(isProductName))
                .toList()
                .get(0);
        assertThat(result.isSuccess(), is(expectedSuccess));
    }

    private RosettaModelObject createFoo(List<Object> bars, int corge) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes,
                "Foo",
                ImmutableMap.of("corge", BigDecimal.valueOf(corge)),
                ImmutableMap.of("bar", bars));
    }
}
