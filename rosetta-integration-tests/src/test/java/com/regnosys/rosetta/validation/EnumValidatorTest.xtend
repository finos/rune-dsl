package com.regnosys.rosetta.validation

import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import javax.inject.Inject
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class EnumValidatorTest implements RosettaIssueCodes {

	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void testEnumNameShouldBeCapitalized() {
		val model =
		'''
			enum quoteRejectReasonEnum:
				Other
		'''.parseRosettaWithNoErrors
		model.assertWarning(ROSETTA_ENUMERATION, INVALID_CASE,
            "Enumeration name should start with a capital")
	}
	
	@Test
	def void testDuplicateEnumValue() {
		val model = '''
			enum Foo:
				BAR
				BAZ
				BAR
		'''.parseRosetta
		model.assertError(ROSETTA_ENUM_VALUE, null, "Duplicate enum value 'BAR'")
	}
	
	@Test
	def void testCannotHaveEnumValuesWithSameNameAsParentValue() {
		val model = '''
		enum A:
			MY_VALUE
		
		enum B extends A:
			MY_VALUE
		'''.parseRosetta
		
		model
			.assertError(ROSETTA_ENUM_VALUE, null, "Duplicate enum value 'MY_VALUE'")
	}
	
	@Test
	def void testParentEnumsCannotHaveACycle() {
		val model = '''
		enum A extends C:
		
		enum B extends A:
		
		enum C extends B:
		'''.parseRosetta
		
		model.assertError(ROSETTA_ENUMERATION, null, "Cyclic extension: A extends C extends B extends A")
		model.assertError(ROSETTA_ENUMERATION, null, "Cyclic extension: B extends A extends C extends B")
		model.assertError(ROSETTA_ENUMERATION, null, "Cyclic extension: C extends B extends A extends C")
	}
	
	@Test
	def void supportDeprecatedAnnotationOnEnum() {
        val model = '''
            enum TestEnumDeprecated:
            	[deprecated]
            	ONE
            	TWO
            
            func Foo:
            	output:
            		result TestEnumDeprecated (1..1)
            	
            	set result:
            		TestEnumDeprecated -> ONE
        '''.parseRosetta

        model.assertWarning(TYPE_CALL, null, "TestEnumDeprecated is deprecated")
        model.assertWarning(ROSETTA_SYMBOL_REFERENCE, null, "TestEnumDeprecated is deprecated")
    }
    
    @Test
    def void supportDeprecatedAnnotationOnEnumValue() {
        val model = '''
            enum TestEnumDeprecated:
            	ONE
            		[deprecated]
            	TWO
            
            func Foo:
            	output:
            		result TestEnumDeprecated (1..1)
            	
            	set result:
            		TestEnumDeprecated -> ONE
        '''.parseRosetta

        model.assertWarning(ROSETTA_FEATURE_CALL, null, "ONE is deprecated")
    }
}