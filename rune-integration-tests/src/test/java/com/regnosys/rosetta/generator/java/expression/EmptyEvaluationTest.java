package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class EmptyEvaluationTest {
    @Inject
    private RosettaTestModelService modelService;

    @Test
    void emptyEqualsEmptyEvaluatesToTrueTest() {
        var model = modelService.toJavaTestModel("").compile();

        boolean result = model.evaluateExpression(Boolean.class, "empty = empty");

        assertTrue(result);
    }

    @Test
    void emptyIfThenElseEvaluatesToNullTest() {
        var model = modelService.toJavaTestModel("").compile();

        Boolean result = model.evaluateExpression(Boolean.class, "if False then True");

        assertNull(result);
    }

    @Test
    void emptyEvaluatesToFalseTest() {
        var model = modelService.toJavaTestModel("").compile();

        boolean result = model.evaluateExpression(Boolean.class, "empty or False");

        assertFalse(result);
    }

    @Test
    void emptyInConstructorEvaluatesToFalseTest() {
        var model = modelService.toJavaTestModel("""
                type Foo:
                    someBoolean boolean (0..1)
                    alwaysFalse boolean (1..1)
                """).compile();

        boolean result = model.evaluateExpression(Boolean.class, "Foo { alwaysFalse: False, ... } -> someBoolean or Foo { alwaysFalse: False, ... } -> alwaysFalse");

        assertFalse(result);
    }
}
