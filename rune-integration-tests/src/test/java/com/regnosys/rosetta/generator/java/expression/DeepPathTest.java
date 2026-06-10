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

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class DeepPathTest {
    @Inject
    private RosettaTestModelService modelService;

    @Test
    void metaKeyIsAccessibleOnChoice() {
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
}
