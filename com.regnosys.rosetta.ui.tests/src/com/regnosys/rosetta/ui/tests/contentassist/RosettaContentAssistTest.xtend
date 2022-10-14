package com.regnosys.rosetta.ui.tests.contentassist

import com.regnosys.rosetta.ui.tests.RosettaUiInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import org.junit.jupiter.api.Disabled

@InjectWith(RosettaUiInjectorProvider)
@ExtendWith(InjectionExtension)
class RosettaContentAssistTest extends ContentAssistTestHelper {

	@Disabled
	@Test
	def void testInheritedAttributesDataRule() {
		'''
		namespace "test"
		version "test"
		type SuperSuperQuote:
			superSuperAttr SuperQuote (0..1)
		
		type SuperQuote extends SuperSuperQuote:
			superAttr SuperQuote (0..1)
		
			condition SomeRule:
				if Quote -> <|>
		
		
		type Quote extends SuperQuote:
			attr Quote (1..1)
		''' >> #["attr", "superAttr", "superSuperAttr"]
	}

	@Disabled // For some reason, Xtext has trouble with proposals if the type `Other` is defined after `Test`.
	// => TODO: Should look into this
	@Test
	def void testDataRuleAfterArrow() {
		'''
			namespace "test"
			version "test"
			
			type Test: otherAttr Other (1..1)
			
			condition CreditDefaultSwap_LongForm:
				if Test -> otherAttr -> testAttr -> otherAttr -> testAttr
				then Test -> otherAttr -> <|>
				
			type Other: testAttr Test (1..1)
		''' >> #["testAttr"]
	}

	@Disabled
	@Test
	def void testDataRuleAfterArrow2() {
		'''
			namespace "test"
			version "test"
			
			type Test: otherAttr Other (1..1)
			
			condition CreditDefaultSwap_LongForm:
				if Test -> otherAttr -> <|>
				
			type Other: testAttr Test (1..1)
		''' >> #["testAttr"]
	}

	@Test
	def void testSynonymSource() {
		'''
			namespace "test"
			
			synonym source FIX
			synonym source FpML
			
			type Foo:
				[synonym <|>]
		''' >= #["FIX", "FpML"]
	}

	@Test
	def void testSynonymSetToEnum() {
		'''
			namespace "test"
			version "test"
			
			type Foo:
				action ActionEnum (1..1)
					[synonym FpML set to ActionEnum -> <|>]
			
			enum ActionEnum: new correct cancel

		''' >= #["cancel", "correct", "new"] // TODO original expectation is not alphabetical sorted but as declared #["new","correct", "cancel"]
	}

	@Test
	def void testSynonymSetToBoolean() {
		'''
			namespace "test"
			version "test"
			
			type Foo:
				attr boolean (1..1)
				[synonym FpML set to T<|>]
			}
		'''  >= #["True"]
	}

	@Test
	def void testAssignOutputEnumLiteral() {
		'''
			namespace "test"
			
			type Quote:
				action ActionEnum (1..1)

			enum ActionEnum:
				new
				correct
				cancel

			enum BadEnum:
				new
				correct
				cancel

			func test:
				inputs: attrIn Quote (0..1)
				output: attrOut Quote (0..1)
				set attrOut -> action:
					<|>
			
		''' >= #['ActionEnum -> cancel', 'ActionEnum -> correct', 'ActionEnum -> new', 'attrIn', 'attrOut', 'test', '"Value"', '(', '<', '[', 'empty', 'False', 'if', 'item', 'True']
	}

	@Test
	def void testImport() {
		'''
			namespace my.ns
			
			import <|>
		'''
		+
		'''namespace my.other.ns''' >= #["my.other.ns.*"] 
	}
}
