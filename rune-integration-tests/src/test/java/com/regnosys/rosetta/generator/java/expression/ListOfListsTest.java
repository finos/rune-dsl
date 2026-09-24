package com.regnosys.rosetta.generator.java.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

/**
 * Tests for expressions that produce a list of lists, in particular when such an expression
 * appears in a branch of a conditional or of a switch.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ListOfListsTest {
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private JavaTypeUtil typeUtil;

    private static final String FOOS = """
            [Foo { xs: ["a", "b"] }, Foo { xs: ["c"] }]
            """;

    @SuppressWarnings("unchecked")
    private List<String> evaluateStringList(JavaTestModel model, String expr) {
        return (List<String>) model.evaluateExpression(typeUtil.wrap(typeUtil.LIST, typeUtil.STRING), expr);
    }

    @Test
    void flattenConditionalContainingListOfLists() {
        JavaTestModel model = modelService.toJavaTestModel("""
                type Foo:
                    xs string (0..*)

                func GetStrings:
                    inputs:
                        foos Foo (0..*)
                        test boolean (1..1)
                    output:
                        result string (0..*)

                    add result:
                        (if test then foos extract item -> xs) flatten
                """).compile();

        assertEquals(List.of("a", "b", "c"), evaluateStringList(model, "GetStrings(" + FOOS + ", True)"));
        assertEquals(List.of(), evaluateStringList(model, "GetStrings(" + FOOS + ", False)"));
    }

    @Test
    void thenFlattenAfterConditionalContainingListOfLists() {
        JavaTestModel model = modelService.toJavaTestModel("""
                type Foo:
                    xs string (0..*)

                func GetStrings:
                    inputs:
                        foos Foo (0..*)
                        test boolean (1..1)
                    output:
                        result string (0..*)

                    add result:
                        if test
                        then foos extract item -> xs
                        then flatten
                """).compile();

        assertEquals(List.of("a", "b", "c"), evaluateStringList(model, "GetStrings(" + FOOS + ", True)"));
        assertEquals(List.of(), evaluateStringList(model, "GetStrings(" + FOOS + ", False)"));
    }

    @Test
    void flattenSwitchContainingListOfLists() {
        JavaTestModel model = modelService.toJavaTestModel("""
                type Foo:
                    xs string (0..*)

                func GetStrings:
                    inputs:
                        foos Foo (0..*)
                        mode string (1..1)
                    output:
                        result string (0..*)

                    add result:
                        (mode switch
                            "all" then foos extract item -> xs,
                            default empty)
                            flatten
                """).compile();

        assertEquals(List.of("a", "b", "c"), evaluateStringList(model, "GetStrings(" + FOOS + ", \"all\")"));
        assertEquals(List.of(), evaluateStringList(model, "GetStrings(" + FOOS + ", \"none\")"));
    }
}
