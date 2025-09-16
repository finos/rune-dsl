package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
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
public class EnumValidatorTest implements RosettaIssueCodes {
    @Inject
    private ValidationTestHelper validationTestHelper;

    @Inject
    private ModelHelper modelHelper;

    @Test
    public void testEnumNameShouldBeCapitalized() {
        RosettaModel model = modelHelper.parseRosettaWithNoErrors("""
            enum quoteRejectReasonEnum:
                Other
            """);
        
        validationTestHelper.assertWarning(model, RosettaPackage.Literals.ROSETTA_ENUMERATION, INVALID_CASE,
            "Enumeration name should start with a capital");
    }

    @Test
    public void testDuplicateEnumValue() {
        RosettaModel model = modelHelper.parseRosetta("""
            enum Foo:
                BAR
                BAZ
                BAR
            """);
        
        validationTestHelper.assertError(model, RosettaPackage.Literals.ROSETTA_ENUM_VALUE, null, 
            "Duplicate enum value 'BAR'");
    }

    @Test
    public void testCannotHaveEnumValuesWithSameNameAsParentValue() {
        RosettaModel model = modelHelper.parseRosetta("""
            enum A:
                MY_VALUE
            
            enum B extends A:
                MY_VALUE
            """);
        
        validationTestHelper.assertError(model, RosettaPackage.Literals.ROSETTA_ENUM_VALUE, null, 
            "Duplicate enum value 'MY_VALUE'");
    }

    @Test
    public void testParentEnumsCannotHaveACycle() {
        RosettaModel model = modelHelper.parseRosetta("""
            enum A extends C:
            
            enum B extends A:
            
            enum C extends B:
            """);
        
        validationTestHelper.assertError(model, RosettaPackage.Literals.ROSETTA_ENUMERATION, null, 
            "Cyclic extension: A extends C extends B extends A");
        validationTestHelper.assertError(model, RosettaPackage.Literals.ROSETTA_ENUMERATION, null, 
            "Cyclic extension: B extends A extends C extends B");
        validationTestHelper.assertError(model, RosettaPackage.Literals.ROSETTA_ENUMERATION, null, 
            "Cyclic extension: C extends B extends A extends C");
    }

    @Test
    public void supportDeprecatedAnnotationOnEnum() {
        RosettaModel model = modelHelper.parseRosetta("""
            enum TestEnumDeprecated:
                [deprecated]
                ONE
                TWO
            
            func Foo:
                output:
                    result TestEnumDeprecated (1..1)
            
                set result:
                    TestEnumDeprecated -> ONE
            """);

        validationTestHelper.assertWarning(model, RosettaPackage.Literals.TYPE_CALL, null, 
            "TestEnumDeprecated is deprecated");
        validationTestHelper.assertWarning(model, ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE, null, 
            "TestEnumDeprecated is deprecated");
    }

    @Test
    public void supportDeprecatedAnnotationOnEnumValue() {
        RosettaModel model = modelHelper.parseRosetta("""
            enum TestEnumDeprecated:
                ONE
                    [deprecated]
                TWO
            
            func Foo:
                output:
                    result TestEnumDeprecated (1..1)
            
                set result:
                    TestEnumDeprecated -> ONE
            """);

        validationTestHelper.assertWarning(model, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL, null, 
            "ONE is deprecated");
    }
}