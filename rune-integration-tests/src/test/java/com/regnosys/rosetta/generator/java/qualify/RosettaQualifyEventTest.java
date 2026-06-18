package com.regnosys.rosetta.generator.java.qualify;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaQualifyEventTest {

    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;
    @Inject
    private QualifyTestHelper qualifyTestHelper;

    private Map<String, Class<?>> classes;

    @BeforeEach
    public void setUp() {
        String code = """
            isEvent root Foo;

            type Foo:
                bar Bar (0..*)
                baz Baz (0..1)

            type Bar:
                before number (0..1)
                after number (0..1)

            type Baz:
                bazValue number (0..1)
                other number (0..1)

            func Qualify_Event:
                [qualification BusinessEvent]
                inputs: foo Foo(1..1)
                output: is_event boolean (1..1)
                set is_event:
                    (foo -> baz -> bazValue is absent or foo -> baz -> bazValue = 15)
            """;
        var generated = codeGeneratorTestHelper.generateCode(code);
        classes = codeGeneratorTestHelper.compileToClasses(generated);
    }

    @Test
    public void whenPresentExpr_isPresent_and_matches_should_qualify() {
        Object bazInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Baz", ImmutableMap.of("bazValue", BigDecimal.valueOf(15)), ImmutableMap.of());
        RosettaModelObject fooInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Foo", ImmutableMap.of("baz", bazInstance), ImmutableMap.of());

        // Assert Event
        List<QualifyResult> results = qualifyTestHelper.createUtilAndGetAllResults(fooInstance);
        QualifyResult result = qualifyTestHelper.getQualifyResult(results, "Event");
        assertTrue(result.isSuccess(), "Unexpected success result");
        assertThat("Unexpected number of expressionDataRule results", result.getExpressionDataRuleResults().size(), is(1));
    }

    @Test
    public void whenPresentExpr_isPresent_and_doesNotMatch_should_not_qualify() {
        Object bazInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Baz", ImmutableMap.of("bazValue", BigDecimal.valueOf(20)), ImmutableMap.of());
        RosettaModelObject fooInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Foo", ImmutableMap.of("baz", bazInstance), ImmutableMap.of());

        // Assert Event
        List<QualifyResult> results = qualifyTestHelper.createUtilAndGetAllResults(fooInstance);
        QualifyResult result = qualifyTestHelper.getQualifyResult(results, "Event");
        assertFalse(result.isSuccess());
        assertThat("Unexpected number of expressionDataRule results", result.getExpressionDataRuleResults().size(), is(1));
    }

    @Test
    public void whenPresentExpr_isNotPresent_should_qualify() {
        Object bazInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Baz", ImmutableMap.of("other", BigDecimal.valueOf(20)), ImmutableMap.of());
        RosettaModelObject fooInstance = codeGeneratorTestHelper.createInstanceUsingBuilder(
                classes, "Foo", ImmutableMap.of("baz", bazInstance), ImmutableMap.of());

        // Assert Event
        List<QualifyResult> results = qualifyTestHelper.createUtilAndGetAllResults(fooInstance);
        QualifyResult result = qualifyTestHelper.getQualifyResult(results, "Event");
        assertTrue(result.isSuccess(), "Unexpected success result");
        assertThat("Unexpected number of expressionDataRule results", result.getExpressionDataRuleResults().size(), is(1));
    }
}
