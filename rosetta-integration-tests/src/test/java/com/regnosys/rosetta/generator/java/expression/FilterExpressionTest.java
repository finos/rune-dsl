package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FilterExpressionTest {
    @Inject
    private RosettaTestModelService modelService;

    @Test
    void filterExpressionTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                
                type Foo:
                    isAllowable boolean (1..1)
                """).compile();

        String result = model.evaluateExpression(String.class, """
                Foo { isAllowable: False } filter isAllowable then "someResult"
                """);

        assertNull(result);
    }
}
