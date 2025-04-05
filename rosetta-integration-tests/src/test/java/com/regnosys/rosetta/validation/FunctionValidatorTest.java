package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ModelHelper;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionValidatorTest {

    @Inject
    private ValidationTestHelper validationTestHelper;
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private ModelHelper modelHelper;
    
    @Test
    void staticFunctionWithNoImplementationShouldWarn() {
        var model = """
            func Foo:
                [staticImplementation]
            """;
      
       var parsed = modelHelper.parseRosetta(model);
       
       validationTestHelper.assertWarning(parsed, FUNCTION, null, "Function has no static implementation present in the model");
    }
}
