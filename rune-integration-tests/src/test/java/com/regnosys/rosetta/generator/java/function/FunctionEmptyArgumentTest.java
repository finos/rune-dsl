package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionEmptyArgumentTest {
    @Inject
    FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    CodeGeneratorTestHelper generatorTestHelper;

    @Test
    void switchOnTypesInAnotherNamespace() {
        var model1 = """
                namespace other
        
                type Baz:
        
                type Foo extends Baz:
                    someBoolean boolean (0..1)
        
                 type Bar extends Baz:
                    someBoolean boolean (0..1)
        """;

        var model2 = """
                import other.* as other
        
                func MyFunc:
                    inputs:
                        baz other.Baz (1..1)
                    output:
                        result string (0..1)
        
                    set result:
                        baz switch
                            other.Foo then "Foo",
                            other.Bar then "Bar",
                            default empty
        """;

        var code = generatorTestHelper.generateCode(new String[]{model1, model2});

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("other"), "Foo", Map.of(
                        "someBoolean", true
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("Foo", result);
    }

    @Test
    void canSetPropertyOnEmptyInputArgument() {
        var model = """
                type Foo:
                    attr1 string (0..1)
                    attr2 string (0..1)
                
                func MyFunc:
                    inputs:
                       inputFoo Foo (0..1)
                       attr2 string (1..1)
                    output:
                       outputFoo Foo (1..1)
                
                    set outputFoo: inputFoo
                
                    set outputFoo -> attr2: attr2
                """;

        
        var code = generatorTestHelper.generateCode(model);
        
        var classes = generatorTestHelper.compileToClasses(code);
        
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, null, "attr2Value");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "attr2", "attr2Value"
        ));

        assertEquals(expected, result);
    }
}
