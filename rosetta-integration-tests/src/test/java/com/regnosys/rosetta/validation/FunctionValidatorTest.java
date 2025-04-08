package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionValidatorTest {
    @Inject
    private ValidationTestHelper validationTestHelper;
    @Inject
    private ModelHelper modelHelper;
    
    
    @Test
    void functionWithNoImplementationAndAnnotationShouldNotWarn() {
        var model = """
            func Foo:
              [codeImplementation]
              output:
                result string (1..1)   
            """;

       var parsed = modelHelper.parseRosetta(model);

       validationTestHelper.assertNoIssues(parsed);
    }
    
    
    @Test
    void functionWithNoImplementationAndNoAnnotationShouldWarn() {
        var model = """
            func Foo:
              output:
                result string (1..1)   
            """;

       var parsed = modelHelper.parseRosetta(model);

       validationTestHelper.assertWarning(parsed, FUNCTION, null, "A function should specify an implementation, or they should be annotated with codeImplementation");
    }

    @Test
    void functionWithCodeImplementationAnnotationAndBodyShouldWarn() {
        var model = """
                func Foo:
                  [codeImplementation]
                  output:
                    result string (1..1)
                    
                  set result: "output"  
                """;

           var parsed = modelHelper.parseRosetta(model);
           
           validationTestHelper.assertWarning(parsed, FUNCTION, null, "Functions annotated with codeImplementation should not have any setter operations as they will be overriden");

    }
}
