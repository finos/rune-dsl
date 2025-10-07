package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
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
    void foo() {
        var model = """
                 type Foo:
                    someBoolean boolean (0..1)
                    alwaysFalse boolean (1..1)
        
                func MyFunc:
                    output:
                        result boolean (1..1)
        
                    set result: Foo { alwaysFalse: False, ... } -> someBoolean or Foo { alwaysFalse: False, ... } -> alwaysFalse
        """;

        var code = generatorTestHelper.generateCode(model);

        generatorTestHelper.writeClasses(code, "foo");

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, Boolean.class);

        assertEquals(false, result);
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
