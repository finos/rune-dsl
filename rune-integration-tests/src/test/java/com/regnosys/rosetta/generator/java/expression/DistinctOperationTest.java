package com.regnosys.rosetta.generator.java.expression;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class DistinctOperationTest {
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private JavaTypeUtil typeUtil;

    @Test
    void distinctRemovesDuplicateIntegersTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                """).compile();

        @SuppressWarnings("unchecked")
        List<BigDecimal> result = (List<BigDecimal>) model.evaluateExpression(typeUtil.wrap(typeUtil.LIST, typeUtil.BIG_DECIMAL), """
                    [1, 5, 1] distinct
                    """);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    void distinctIgnoresPrecisionTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                """).compile();

        // In the Rune DSL precision is not part of a number's identity, so `1.0` and `1.00`
        // are the same number and should be collapsed by `distinct`.
        @SuppressWarnings("unchecked")
        List<BigDecimal> result = (List<BigDecimal>) model.evaluateExpression(typeUtil.wrap(typeUtil.LIST, typeUtil.BIG_DECIMAL), """
                    [1.0, 5.0, 1.00] distinct
                    """);

        Assertions.assertEquals(2, result.size());
    }
}
