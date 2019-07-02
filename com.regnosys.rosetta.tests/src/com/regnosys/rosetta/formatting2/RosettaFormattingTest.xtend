package com.regnosys.rosetta.formatting2

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.resource.SaveOptions
import org.eclipse.xtext.serializer.ISerializer
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaFormattingTest {

	@Inject extension ParseHelper<RosettaModel>
	@Inject extension ISerializer

	@Test
	def void simpleClassWithOneFieldIsFormatted() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			class Test <"Some definition">
			{
				field1 string (1..1) <"Field 1">;
				field2 string (1..1) <"Field 2">;
			}
			
		'''

		val unFormatted = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			class Test <"Some definition"> { 	field1 string (1..1) <"Field 1">; field2 string (1..1) <"Field 2">; }			}		
		'''

		assertEquals(expectedResult, format(unFormatted))
	}

	@Test
	def void stereotypeIsSurroundedWithSingleSpace() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			class CalculationPeriod stereotype contractualProduct, entityReferenceData <"xxx xxx.">
				[synonym FpML value CalculationPeriod]
			{
			}
			
		'''

		val unFormatted = '''
			namespace "com.regnosys.rosetta.model" version "test" class CalculationPeriod stereotype contractualProduct,entityReferenceData <"xxx xxx."> 
			 [synonym FpML value CalculationPeriod]
			{
			}
		'''

		assertEquals(expectedResult, format(unFormatted))
	}

	@Test
	def void synonymIsOnSingleIndentedLine() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			class CalculationPeriod stereotype contractualProduct <"xxx xxx.">
				[synonym FpML value CalculationPeriod]
			{
				field1 string (1..1) <"Some Field">;
					[synonym FpML value CalculationPeriod]
			}
			
		'''

		val unFormatted = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			class CalculationPeriod stereotype contractualProduct <"xxx xxx."> [synonym FpML value CalculationPeriod]
			{
								field1 string (1..1) <"Some Field">;[synonym FpML value CalculationPeriod]
				
			}
			
		'''

		assertEquals(expectedResult, format(unFormatted))
	}

	@Test
	def void classWithProductReferenceDataIsFormattedWithClassAndAttributes() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			class Product stereotype productReferenceData <"Product Def.">
				[regulatoryReference ESMA MiFIR article "Article 1" provision "Some provision"]
			{
				field1 string (1..1) <"Some Field">;
					[regulatoryReference ESMA MiFIR article "Article 2" provision "Another provision"]
				field2 string (1..1) <"Some Field">;
			}
			
		'''
		val unFormatted = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			class Product stereotype productReferenceData <"Product Def."> [regulatoryReference ESMA MiFIR article "Article 1" provision "Some provision"]  {
			field1 string (1..1) <"Some Field">;
			[regulatoryReference ESMA MiFIR article "Article 2" provision "Another provision"] field2 string (1..1) <"Some Field">;
			
			}
		'''
		assertEquals(expectedResult, format(unFormatted))
	}

	@Test
	def void formatDatesReferenceEnum() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			enum DatesReferenceEnum <"The enumerated values to specify the set of dates that can be referenced through FpML href constructs of type ...periodDatesReference.">
			{
				tradeDate,
				effectiveDate,
				firstRegularPeriodStartDate,
				lastRegularPeriodEndDate,
				cashSettlementPaymentDate <"FpML business validation rule ird-46 specifies that, in the context of the OptionalEarlyTermination, cashSettlement/cashSettlementValuationDate/dateRelativeTo/@href must equal cashSettlement/cashSettlementPaymentDate/@id.">,
				calculationPeriodDates <"FpML business validation rule ird-59 specifies that if resetDates exists in resetDates/calculationPeriodDatesReference, the @href attribute is equal to the @id attribute of calculationPeriodDates in the same swapStream.">,
				paymentDates,
				resetDates,
				fixingDates,
				valuationDates
			}
			
		'''
		val unFormatted = '''
			namespace "com.regnosys.rosetta.model" version "test" enum DatesReferenceEnum <"The enumerated values to specify the set of dates that can be referenced through FpML href constructs of type ...periodDatesReference.">
			{
				tradeDate,
				effectiveDate,firstRegularPeriodStartDate,lastRegularPeriodEndDate,cashSettlementPaymentDate <"FpML business validation rule ird-46 specifies that, in the context of the OptionalEarlyTermination, cashSettlement/cashSettlementValuationDate/dateRelativeTo/@href must equal cashSettlement/cashSettlementPaymentDate/@id.">,
				calculationPeriodDates <"FpML business validation rule ird-59 specifies that if resetDates exists in resetDates/calculationPeriodDatesReference, the @href attribute is equal to the @id attribute of calculationPeriodDates in the same swapStream.">,
				paymentDates,resetDates,fixingDates,valuationDates
			}
				
		'''
		assertEquals(expectedResult, format(unFormatted))
	}

	@Test
	def void formatDayOfWeekEnum() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			enum DayOfWeekEnum <"A day of the seven-day week.">
				[synonym FpML value DayOfWeekEnum]
			{
				MON <"Monday">
					[synonym FpML value "MON"],
				TUE <"Tuesday">
					[synonym FpML value "TUE"],
				WED <"Wednesday">
					[synonym FpML value "WED"],
				THU <"Thursday">
					[synonym FpML value "THU"],
				FRI <"Friday">
					[synonym FpML value "FRI"],
				SAT <"Saturday">
					[synonym FpML value "SAT"],
				SUN <"Sunday">
					[synonym FpML value "SUN"]
			}
			
		'''
		val unFormatted = '''namespace "com.regnosys.rosetta.model" version "test" enum DayOfWeekEnum <"A day of the seven-day week."> [synonym FpML value DayOfWeekEnum]{MON <"Monday">[synonym FpML value "MON"],TUE <"Tuesday">[synonym FpML value "TUE"],WED <"Wednesday">[synonym FpML value "WED"],THU <"Thursday">[synonym FpML value "THU"],FRI <"Friday">[synonym FpML value "FRI"],SAT <"Saturday">[synonym FpML value "SAT"],SUN <"Sunday">[synonym FpML value "SUN"]}'''
		assertEquals(expectedResult, format(unFormatted))
	}
	
	@Test
	def void formatSpreadScheduleTypeEnum() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			enum SpreadScheduleTypeEnum <"The enumerated values to specify a long or short spread value.">
				[synonym FpML value spreadScheduleTypeScheme]
			{
				Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
					[synonym FpML value "Long"],
				Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
					[synonym FpML value "Short"]
			}
			
		'''
		val unFormatted = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			enum SpreadScheduleTypeEnum <"The enumerated values to specify a long or short spread value."> 	[synonym FpML value spreadScheduleTypeScheme]
			{
				Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
			[synonym FpML value "Long"], Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
					[synonym FpML value "Short"]
			}
			
		'''
		assertEquals(expectedResult, format(unFormatted))
	}
	
	@Test
	def void formatDataRule() {
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			enum SpreadScheduleTypeEnum <"The enumerated values to specify a long or short spread value.">
				[synonym FpML value spreadScheduleTypeScheme]
			{
				Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
					[synonym FpML value "Long"],
				Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
					[synonym FpML value "Short"]
			}
			
		'''
		val unFormatted = '''
			namespace "com.regnosys.rosetta.model" 
			version "test"
			enum SpreadScheduleTypeEnum <"The enumerated values to specify a long or short spread value."> 	
				[synonym FpML value spreadScheduleTypeScheme]
			{
				Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
			[synonym FpML value "Long"],
			Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
					[synonym FpML value "Short"]
			}
			
		'''
		assertEquals(expectedResult, format(unFormatted))
	}


	@Test
	def void formatExternalSynomym() {
		val unFormatted = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			synonym source FpML_5_10
			
			synonym source MySynonymExtention extends FpML_5_10 {
				
				
				Contract:
				+ account	[value my_account]
				[value my_account_2]
				
				+ party [value my_party]
				
				
				Event:
					+ account
					[value event_account]
					
					
					
			}
		'''
		val expectedResult = '''
			namespace "com.regnosys.rosetta.model"
			version "test"
			
			synonym source FpML_5_10
			
			synonym source MySynonymExtention extends FpML_5_10
			{
				Contract:
					+ account
						[value my_account]
						[value my_account_2]
					+ party
						[value my_party]
			
				Event:
					+ account
						[value event_account]
			}
			
		'''
		assertEquals(expectedResult, format(unFormatted))
	}
	
	def String format(String unFormatted) {
		unFormatted.parse.serialize(SaveOptions.newBuilder.format().getOptions())
	}

}
