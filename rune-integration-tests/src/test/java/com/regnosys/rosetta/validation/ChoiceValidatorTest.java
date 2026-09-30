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
    public void testNestedChoiceOptionsSharingALeafTypeWarns() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice OuterChoice:
                LeftChoice
                RightChoice

            choice LeftChoice:
                Amount
                LeftOnly

            choice RightChoice:
                Amount
                RightOnly

            type Amount:

            type LeftOnly:

            type RightOnly:
            """);

        validationTestHelper.assertWarning(model, SimplePackage.Literals.CHOICE_OPTION, null,
            "'Amount' is included by both 'LeftChoice' and 'RightChoice', so deserialisation cannot tell which one it came from");
    }

    @Test
    public void testDeeplyNestedChoiceOptionsSharingALeafTypeWarns() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice OuterChoice:
                LeftChoice
                RightChoice

            choice LeftChoice:
                Inner
                LeftOnly

            choice Inner:
                Amount

            choice RightChoice:
                Amount

            type Amount:

            type LeftOnly:
            """);

        validationTestHelper.assertWarning(model, SimplePackage.Literals.CHOICE_OPTION, null,
            "'Amount' is included by both 'LeftChoice' and 'RightChoice', so deserialisation cannot tell which one it came from");
    }

    @Test
    public void testNestedChoiceOptionsWithDistinctLeafTypesDoNotWarn() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice OuterChoice:
                LeftChoice
                RightChoice

            choice LeftChoice:
                LeftOnly

            choice RightChoice:
                RightOnly

            type LeftOnly:

            type RightOnly:
            """);

        validationTestHelper.assertNoIssues(model);
    }

    @Test
    public void testNestedChoiceOptionsWithDistinctAliasesOfTheSameTypeDoNotWarn() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice OuterChoice:
                LeftChoice
                RightChoice

            choice LeftChoice:
                AliasA

            choice RightChoice:
                AliasB

            typeAlias AliasA: string

            typeAlias AliasB: string
            """);

        validationTestHelper.assertNoIssues(model);
    }

    @Test
    public void testNestedChoiceOptionsSharingABasicTypeWithDifferentParametersWarns() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice OuterChoice:
                LeftChoice
                RightChoice

            choice LeftChoice:
                number(digits: 3)

            choice RightChoice:
                number(digits: 5)
            """);

        validationTestHelper.assertWarning(model, SimplePackage.Literals.CHOICE_OPTION, null,
            "'number' is included by both 'LeftChoice' and 'RightChoice', so deserialisation cannot tell which one it came from");
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
    public void testChoiceAliasOfNestedChoiceOptionsDoNotOverlap() {
        RosettaModel model = modelHelper.parseRosetta("""
            choice Outer:
                InnerAlias
                TargetOpt

            typeAlias InnerAlias: Inner

            choice Inner:
                TargetOpt
                Sibling

            type TargetOpt:

            type Sibling:
            """);

        validationTestHelper.assertError(model, SimplePackage.Literals.CHOICE_OPTION, null,
            "Option 'TargetOpt' is already included by option 'InnerAlias'");
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