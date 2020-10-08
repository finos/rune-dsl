package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaQualifiable
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
	def testAliasStackOverflow() {
		val rule = '''
			type Foo:
				bar string (0..*)
			
			alias A <"...">
				Foo -> bar 
				or A
		'''.parseRosettaWithNoErrors.elements.filter(RosettaAlias).head

		assertEquals('Can not compute type for A because of recursive call.', rule.RType.name)
	}
	
	@Test
	def void testEnumCompatibility() {
		'''
			namespace "test"
			version "test"
			
			enum Enumerate: X Y Z
			enum EnumerateExtended extends Enumerate: A  B  C
			
			type Prodtype:
				attr Enumerate (0..1)
				attrEx EnumerateExtended (0..1)
			
			isProduct Prod 
				Prodtype -> attrEx = Enumerate -> X
		'''.parseRosettaWithNoErrors
	}

	@Test
	def testBinaryExpressionCommonType() {
		val aliases = '''
			type Foo:
				iBar int (0..*)
				nBar number (0..*)
				nBuz number (0..*)
			
			isEvent AllNumber 
				(Foo -> nBar or Foo -> nBuz) = 4.0
				
			isEvent MixedNumber 
				(Foo -> nBar or Foo -> iBar) = 4.0
		'''.parseRosettaWithNoErrors.elements.filter(RosettaQualifiable)
		val alNumber = aliases.filter[name == "AllNumber"].head
		assertEquals('number', (alNumber.expression as RosettaBinaryOperation).left.RType.name)
		val mixed = aliases.filter[name == "MixedNumber"].head
		assertEquals('number', (mixed.expression as RosettaBinaryOperation).left.RType.name)
	}
}
