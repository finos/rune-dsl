package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import javax.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ChoiceValidatorTest implements RosettaIssueCodes {
    @Inject
    private ValidationTestHelper validationTestHelper;

    @Inject
    private ModelHelper modelHelper;

    @Test
    public void testChoiceOptionsDoNotOverlap() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice Foo:
                Opt1
                Nested
            
            type Opt1:
            
            choice Nested:
                Opt1
                Opt2
            
            type Opt2:
            """);
        
        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null, 
            "Option 'Opt1' is already included by option 'Nested'");
    }

    @Test
    public void testNoCircularReferenceInChoiceOptions() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice Foo:
                Opt1
                Bar
            
            type Opt1:
            
            choice Bar:
                Foo
            """);
        
        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null, 
            "Cyclic option: Foo includes Bar includes Foo");
        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null, 
            "Cyclic option: Bar includes Foo includes Bar");
    }

    @Test
    public void supportDeprecatedAnnotationOnChoice() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice FooDeprecated:
                [deprecated]
                string
                int
            
            func Foo:
                output:
                    result FooDeprecated (1..1)
            
                set result:
                    FooDeprecated { string: "My string", ... }
            """);

        validationTestHelper.assertWarning(model, RosettaPackage.Literals.TYPE_CALL, null, 
            "FooDeprecated is deprecated");
    }
}