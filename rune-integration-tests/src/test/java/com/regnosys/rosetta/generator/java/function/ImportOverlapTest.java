package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ImportOverlapTest {
    @Inject
    FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    CodeGeneratorTestHelper generatorTestHelper;

    @Test
    void canImportOverlappingNamedFunctions() {
        var model1 = """
                namespace other
                
                func OtherFunc:
                    output:
                        result string (1..1)
                
                    set result: "DependencyOtherFuncValue"
                """;

        var model2 = """
                import other.* as dep
                
                func OtherFunc:
                    output:
                        result string (1..1)
                
                    set result: "OtherFuncValue"
                
                func MyFunc:
                    output:
                       result string (1..1)
                
                    set result: OtherFunc() + " " + dep.OtherFunc()
                """;


        var code = generatorTestHelper.generateCode(new String[]{model1, model2});

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class);

        var expected = "OtherFuncValue DependencyOtherFuncValue";

        assertEquals(expected, result);
    }
}
