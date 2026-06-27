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
    void deepFeatureCallOnAliasOfChoiceShouldResolve() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                type OptionA:
                    sharedAttr string (0..1)

                type OptionB:
                    sharedAttr string (0..1)

                choice SomeChoice:
                    OptionA
                    OptionB

                typeAlias ChoiceAlias: SomeChoice

                type Container:
                    field ChoiceAlias (0..1)
                """).compile();

        String result = model.evaluateExpression(String.class, """
                Container { field: SomeChoice { OptionA: OptionA { sharedAttr: "hello" }, ... } }
                    -> field ->> sharedAttr
                """);
        assertEquals("hello", result);
    }
}
