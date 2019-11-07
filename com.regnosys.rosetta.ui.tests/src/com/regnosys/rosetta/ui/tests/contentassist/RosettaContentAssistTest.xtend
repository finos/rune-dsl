package com.regnosys.rosetta.ui.tests.contentassist

import com.regnosys.rosetta.ui.tests.RosettaUiInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

@InjectWith(RosettaUiInjectorProvider)
@ExtendWith(InjectionExtension)
class RosettaContentAssistTest extends AbstractContentAssistTest {

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
			
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
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
	def void testProductExpressionEnumLiteral() {
		'''
			type Quote:
				action ActionEnum (1..1)
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
			isProduct test
				Quote -> action <> <|>
		''' >= #['ActionEnum -> cancel', 'ActionEnum -> correct', 'ActionEnum -> new', '"Value"', "(", "[", "empty", "False", "True"]
	}
	
	@Test
	def void testProductExpressionEnumLiteral2() {
		'''
			type Quote:
				action ActionEnum (1..1)
			}
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
			isProduct test
				<|>
		''' >= #['"Value"', "(", "<", "[", "empty", "False", "True"]
	}
	
	@Test
	def void testAssignOutputEnumLiteral() {
		'''
			type Quote:
				action ActionEnum (1..1)

			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
			enum BadEnum
			{
				new,
				correct,
				cancel
			}
			func test:
				inputs: attrIn Quote (0..1)
				output: attrOut Quote (0..1)
				assign-output attrOut -> action:
					<|>
			
		''' >= #['ActionEnum -> cancel', 'ActionEnum -> correct', 'ActionEnum -> new', 'attrIn', 'attrOut', 'test','"Value"', "(", "<", "[", "empty", "False","if", "True"]
	}

	@Test
	def void testIsProduct() {
		'''
			isProduct root EconomicTerms;

			type EconomicTerms:
			
			type Quote:
				attr String (1..1)
			
			
			isProduct test
				<|>
		''' >= #['EconomicTerms', '"Value"', "(", "<", "[", "empty", "False", "True"]
		'''
			isProduct root EconomicTerms;
			
			type EconomicTerms:
			
			type Quote:
				attr String (1..1)
			
			type EconomicTerms:
				ecoTermsAttr String (1..1)
			
			isProduct test
				EconomicTerms or <|>
		''' >= #['EconomicTerms', '"Value"', "(", "[",  "empty", "False", "True"]
	}

	@Test
	def void testIsEvent() {
		'''
			isEvent root Event;
			
			type Event:
			
			type Quote:
				attr String (1..1)

			isProduct root EconomicTerms {
				ecoTermsAttr String (1..1);
			}
			
			alias EventAlias 
				Event -> eventAttr
			
			alias ProductAlias 
				EconomicTerms -> ecoTermsAttr
			
			isEvent test
				<|>
		''' >= #['Event', 'EventAlias', '"Value"', "(", "<", "[", "empty", "False", "True"] 
		
		'''
			isEvent root Event;
			
			type Event:
			
			type Quote:
				attr String (1..1)
			
			type Event:
				eventAttr String (1..1)
			
			alias EventAlias 
				Event -> eventAttr
			
			isEvent test
				Event and <|>
		''' >= #['Event', 'EventAlias', '"Value"', "(", "[", "empty", "False", "True"]  
	}
	
}
