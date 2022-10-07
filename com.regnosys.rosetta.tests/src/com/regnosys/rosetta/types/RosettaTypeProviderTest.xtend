package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypeProviderTest {

	@Inject extension RosettaTypeProvider
	@Inject extension ModelHelper modelHelper
	
	@Test
	def void testEnumCompatibility() {
		'''
			namespace "test"
			version "test"
			
			isProduct root ProdType;
			
			enum Enumerate: X Y Z
			enum EnumerateExtended extends Enumerate: A  B  C
			
			type ProdType:
				attr Enumerate (0..1)
				attrEx EnumerateExtended (0..1)
			
			func Qualify_Prod:
				[qualification Product]
				inputs: prodType ProdType (1..1)
				output: is_product boolean (1..1)
				set is_product:
					prodType -> attrEx = Enumerate -> X
		'''.parseRosettaWithNoErrors
	}

	@Test
	def void testBinaryExpressionCommonType() {
		val funcs = '''
			isEvent root Foo;
			
			type Foo:
				iBar int (0..*)
				nBar number (0..*)
				nBuz number (0..*)
			
			func Qualify_AllNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> nBuz] contains 4.0
			
			func Qualify_MixedNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> iBar] contains 4.0
			
			func Qualify_IntOnly:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					foo -> iBar = 4.0
		'''.parseRosettaWithNoErrors.elements.filter(Function)
		
		val allNumber = funcs.filter[name == "Qualify_AllNumber"].head
		assertEquals('number', (allNumber.operations.head.expression as RosettaContainsExpression).left.RType.name)
		val mixed = funcs.filter[name == "Qualify_MixedNumber"].head
		assertEquals('number', (mixed.operations.head.expression as RosettaContainsExpression).left.RType.name)
		val intOnly = funcs.filter[name == "Qualify_IntOnly"].head
		assertEquals('int', (intOnly.operations.head.expression as RosettaBinaryOperation).left.RType.name)
	}
}
