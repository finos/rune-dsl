package com.regnosys.rosetta.generator.java.types;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JavaTypeTranslatorTest {
    @Inject
    private RosettaTestModelService modelService;

    private Class<?> barClass;

    @BeforeAll
    void setup() {
        JavaTestModel model = modelService.toJavaTestModel("""
                type FooWithKey:
                	[metadata key]
                
                type FooWithBoth:
                	[metadata key]
                	[metadata template]
                
                type Bar:
                    attr1 FooWithKey (1..1)
                    attr2 FooWithBoth (1..1)
                    attr3 FooWithKey (1..1)
                        [metadata scheme]
                    attr4 FooWithBoth (1..1)
                        [metadata scheme]
                """).compile();
        barClass = model.getTypeJavaClass("Bar");
    }

    List<TestCase> getTestCases() {
        return List.of(
                new TestCase("attr1", "FooWithKey"),
                new TestCase("attr2", "FooWithBoth"),
                new TestCase("attr3", "FieldWithMetaFooWithKey"),
                new TestCase("attr4", "FieldWithMetaFooWithBoth")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void testAttributeType(TestCase testCase) {
        Assertions.assertEquals(
                testCase.expectedJavaType,
                getMethodType(testCase.attribute).getSimpleName());
    }

    private Class<?> getMethodType(String attributeName) {
        Method method;
        try {
            method = barClass.getMethod("get" + StringUtils.capitalize(attributeName));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return method.getReturnType();
    }

    private record TestCase(String attribute, String expectedJavaType) {
        @Override
        public String toString() {
            return "Attribute " + attribute + " should have Java type " + expectedJavaType;
        }
    }
}
