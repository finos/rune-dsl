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
			class SuperSuperQuote {
				superSuperAttr SuperQuote (0..1);
			}
			class SuperQuote extends SuperSuperQuote{
				superAttr SuperQuote (0..1);
			}
			
			data rule SomeRule
				when Quote -> <|>
			
			class Quote extends SuperQuote
			{
				attr Quote (1..1);
			}
		''' >> #["attr", "superAttr", "superSuperAttr"]
	}

	@Test
	def void testDataRuleAfterArrow() {
		'''
			version "test"
			
			class Test { otherAttr Other (1..1); }
			
			data rule CreditDefaultSwap_LongForm 
				when Test -> otherAttr -> testAttr -> otherAttr -> testAttr
				then Test -> otherAttr -> <|>
				
			class Other { testAttr Test (1..1); }
		''' >> #["testAttr"]
	}

	@Test
	def void testDataRuleAfterArrow2() {
		'''
			version "test"
			
			class Test { otherAttr Other (1..1); }
			
			data rule CreditDefaultSwap_LongForm 
				when Test -> otherAttr -> <|>
				
			class Other { testAttr Test (1..1); }
		''' >> #["testAttr"]
	}

	@Test
	def void testSynonymSource() {
		'''
			synonym source FIX
			synonym source FpML
			
			class Foo
				[synonym <|>]
			{}
		''' >> #["FIX", "FpML"]
	}

	@Test
	def void testSynonymSetToEnum() {
		'''
			namespace "test"
			version "test"
			
			class Foo
			{
				action ActionEnum (1..1);
					[synonym FpML set to ActionEnum.<|>]
			}
			
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
		''' >> #["cancel", "correct", "new", "."] // TODO original expectation is not alphabetical sorted but as declared #["new","correct", "cancel"]
	}

	@Test
	def void testSynonymSetToBoolean() {
		'''
			namespace "test"
			version "test"
			
			class Foo
			{
				attr boolean (1..1);
					[synonym FpML set to T<|>]
			}
		''' >> #["True", "."]
	}

	@Test
	def void testProductExpressionEnumLiteral() {
		'''
			class Quote
			{
				action ActionEnum (1..1);
			}
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
			isProduct test
				Quote -> action <> <|>
		''' >> #['ActionEnum.cancel', 'ActionEnum.correct', 'ActionEnum.new', '"Value"', "(", "[", "empty", "False", "True"]
	}
	
	@Test
	def void testProductExpressionEnumLiteral2() {
		'''
			class Quote
			{
				action ActionEnum (1..1);
			}
			enum ActionEnum
			{
				new,
				correct,
				cancel
			}
			isProduct test
				<|>
		''' >> #['"Value"', "(", "<", "[", "empty", "False", "True"]
	}

	@Test
	def void testIsProduct() {
		'''
			isProduct root EconomicTerms;

			class EconomicTerms {
			}
			
			class Quote
			{
				attr String (1..1);
			}
			
			
			isProduct test
				<|>
		''' >> #['EconomicTerms', '"Value"', "(", "<", "[", "empty", "False", "True"]
		'''
			isProduct root EconomicTerms;
			
			class EconomicTerms {
			}
			
			class Quote
			{
				attr String (1..1);
			}
			
			class EconomicTerms {
				ecoTermsAttr String (1..1);
			}
			
			isProduct test
				EconomicTerms or <|>
		''' >> #['EconomicTerms', '"Value"', "(", "[",  "empty", "False", "True"]
	}

	@Test
	def void testIsEvent() {
		'''
			isEvent root Event;
			
			class Event {}
			
			class Quote
			{
				attr String (1..1);
			}
			isProduct root EconomicTerms {
				ecoTermsAttr String (1..1);
			}
			
			alias EventAlias 
				Event -> eventAttr
			
			alias ProductAlias 
				EconomicTerms -> ecoTermsAttr
			
			isEvent test
				<|>
		''' >> #['Event', 'EventAlias', '"Value"', "(", "<", "[", "empty", "False", "True"] 
		
		'''
			isEvent root Event;
			
			class Event {}
			
			class Quote
			{
				attr String (1..1);
			}
			
			class Event {
				eventAttr String (1..1);
			}
			
			alias EventAlias 
				Event -> eventAttr
			
			isEvent test
				Event and <|>
		''' >> #['Event', 'EventAlias', '"Value"', "(", "[", "empty", "False", "True"]  
	}
	
}
