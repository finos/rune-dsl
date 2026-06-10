package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behavioural tests for the implied {@code [metadata key]} on a choice, exercised by evaluating the {@code ->> key}
 * deep-feature operator and asserting the resolved key. The cases where {@code ->> key} must <em>not</em> link
 * (because the choice has no implied key) live in {@code RosettaValidatorTest}.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class DeepPathTest {
    @Inject
    private RosettaTestModelService modelService;

    @Test
    void keyResolvesWhenEveryLeafIsKeyed() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                metaType key string

                type OptionA:
                  [metadata key]
                    fieldA string (1..1)

                type OptionB:
                  [metadata key]
                    fieldB string (1..1)

                choice SomeChoice:
                    OptionA
                    OptionB
                """).compile();

        String result = model.evaluateExpression(String.class, """
                SomeChoice { OptionA: OptionA { fieldA: "test" } with-meta { key: "someKey"}, ... } ->> key
                """);

        assertEquals("someKey", result);
    }

    @Test
    void keyResolvesWhenLeafInheritsKeyFromSupertype() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                metaType key string

                type Base:
                  [metadata key]
                    field string (1..1)

                type OptionA extends Base:
                type OptionB extends Base:

                choice SomeChoice:
                    OptionA
                    OptionB
                """).compile();

        String result = model.evaluateExpression(String.class, """
                SomeChoice { OptionA: OptionA { field: "test" } with-meta { key: "someKey"}, ... } ->> key
                """);

        assertEquals("someKey", result);
    }

    @Test
    void keyResolvesThroughNestedChoice() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                metaType key string

                type OptionA:
                  [metadata key]
                    fieldA string (1..1)

                type OptionB:
                  [metadata key]
                    fieldB string (1..1)

                type OptionC:
                  [metadata key]
                    fieldC string (1..1)

                choice InnerChoice:
                    OptionA
                    OptionB

                choice OuterChoice:
                    InnerChoice
                    OptionC
                """).compile();

        String fromNestedChoice = model.evaluateExpression(String.class, """
                OuterChoice { InnerChoice: InnerChoice { OptionA: OptionA { fieldA: "test" } with-meta { key: "innerKey"}, ... }, ... } ->> key
                """);
        assertEquals("innerKey", fromNestedChoice);

        String fromDirectLeaf = model.evaluateExpression(String.class, """
                OuterChoice { OptionC: OptionC { fieldC: "test" } with-meta { key: "leafKey"}, ... } ->> key
                """);
        assertEquals("leafKey", fromDirectLeaf);
    }
}
