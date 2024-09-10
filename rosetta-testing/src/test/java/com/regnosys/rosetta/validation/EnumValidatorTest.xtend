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

@ExtendWith(InjectionExtension)
@InjectWith(MyRosettaInjectorProvider)
class EnumValidatorTest implements RosettaIssueCodes {

	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
	
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
	def void testCannotExtendEnumsWithEnumValuesWithSameName() {
		val model = '''
		enum A:
			MY_VALUE
		
		enum B:
			MY_VALUE
		
		enum C extends A, B:
		'''.parseRosetta
		
		model
			.assertError(ROSETTA_ENUMERATION, null, "The value 'MY_VALUE' of enum B overlaps with the value 'MY_VALUE' of enum A")
	}
	
	@Test
	def void testParentEnumsCannotHaveACycle() {
		val model = '''
		enum A extends D:
		
		enum B extends A:
		
		enum C extends A:
		
		enum D extends B, C:
		'''.parseRosetta
		
		model
			.assertError(ROSETTA_ENUMERATION, null, "AAAAAAAAAA")
	}
}