package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
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
	def testAttributes() {
		val rule = '''
			class Foo {
				foo Foo (0..1);
				bar boolean (1..1);
			}
			
			data rule DataRule
				when Foo -> foo exists
				then Foo -> bar = True
		'''.parseRosettaWithNoErrors.elements.filter(RosettaDataRule).head

		val when = rule.when as RosettaExistsExpression
		assertEquals('boolean', when.RType.name)
		assertEquals('featureCall(Foo)', when.argument.RType.name)

		val then = rule.then as RosettaBinaryOperation
		assertEquals('boolean', then.RType.name)
		assertEquals('boolean', then.right.RType.name)
		assertEquals('boolean', then.left.RType.name)
	}

	@Test
	def testEnumsAndBooleanOperations() {
		val rule = '''
			class Foo {
				bar Bar (0..*);
			}
			
			enum Bar {
				X,Y
			}
			
			data rule DataRule 
				when Foo->bar contains Bar->X and Foo->bar contains Bar->Y
				then Foo->bar exists
		'''.parseRosettaWithNoErrors.elements.filter(RosettaDataRule).head

		val when = rule.when as RosettaBinaryOperation
		assertEquals('boolean', when.RType.name)
		assertEquals('boolean', when.left.RType.name)

		val left = (when.left as RosettaContainsExpression)
		assertEquals('Bar', left.container.RType.name)
		assertEquals('Bar', left.contained.RType.name)
	}

	@Test
	def testLiterals() {
		val rule = '''
			data rule r 
				when -1 = True
				then 1.1 = .0
		'''.parseRosetta.elements.last as RosettaDataRule

		val when = rule.when as RosettaBinaryOperation
		assertEquals('int', when.left.RType.name)
		assertEquals('boolean', when.right.RType.name)

		val then = rule.then as RosettaBinaryOperation
		assertEquals('number', then.left.RType.name)
		assertEquals('number', then.right.RType.name)
	}


	@Test
	def testAliasStackOverflow() {
		val rule = '''
			class Foo {
				bar string (0..*);
			}
			
			
			alias A <"...">
				Foo -> bar 
				or A
		'''.parseRosettaWithNoErrors.elements.filter(RosettaAlias).head

		assertEquals('Can not compute type for A because of recursive call.', rule.RType.name)
	}

	@Test
	def testBinaryExpressionCommonType() {
		val aliases = '''
			class Foo {
				iBar int (0..*);
				nBar number (0..*);
				nBuz number (0..*);
			}
			
			
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
