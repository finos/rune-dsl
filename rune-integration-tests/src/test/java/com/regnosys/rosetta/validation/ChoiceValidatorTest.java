package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import javax.inject.Inject;

import org.eclipse.xtext.diagnostics.Severity;
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
    public void testChoiceOptionsDoNotInheritEachOther() {
        RosettaModel model = modelHelper.parseRosetta("""
            type Base:
            
            type Foo extends Base:
            
            choice SomeChoice:
                Base
                Foo
            """);

        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null,
                "Option 'Foo' is in the same type hierarchy as 'Base'");

        RosettaModel model2 = modelHelper.parseRosetta("""
            type Base:
            
            type Foo extends Base:
            
            choice SomeChoice:
                Foo
                Base
            """);

        validationTestHelper.assertError(model2, SimplePackage.Literals.CHOICE_OPTION, null,
                "Option 'Base' is in the same type hierarchy as 'Foo'");
    }

    @Test
    public void testChoiceOptionsDoNotInheritEachOtherIndirectExtension() {
        RosettaModel model = modelHelper.parseRosetta("""
            type Base:
            
            type Mid extends Base
            
            type Foo extends Mid:
            
            choice SomeChoice:
                Base
                Foo
            """);

        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null,
                "Option 'Foo' is in the same type hierarchy as 'Base'");

        RosettaModel model2 = modelHelper.parseRosetta("""
            type Base:
            
            type Mid extends Base
            
            type Foo extends Mid:
            
            choice SomeChoice:
                Foo
                Base
            """);

        validationTestHelper.assertError(model2, SimplePackage.Literals.CHOICE_OPTION, null,
                "Option 'Base' is in the same type hierarchy as 'Foo'");
    }

    @Test
    public void testSiblingTypeChoiceOptionsAreValid() {
        RosettaModel model = modelHelper.parseRosetta("""
            type Base:
            
            type Bar extends Base:
            
            type Foo extends Base:
            
            choice SomeChoice:
                Bar
                Foo
            """);

        validationTestHelper.assertNoIssues(model);
    }

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

        validationTestHelper.assertIssue(model, RosettaPackage.Literals.TYPE_CALL, null, Severity.INFO, "FooDeprecated is deprecated");
    }
}