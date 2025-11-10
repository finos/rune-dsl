package com.regnosys.rosetta.formatting2;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaFormattingTest {

	@Inject
	private FormatterTestHelper formatterTestHelper;

	private void formatAndAssert(CharSequence unformatted, CharSequence expectation) {
		formatterTestHelper.assertFormatted(cfg -> {
			cfg.setExpectation(expectation.toString().replace("\n", System.lineSeparator()));
			cfg.setToBeFormatted(unformatted);

			// extra check to make sure we didn't miss any hidden region in our formatter:
			cfg.setAllowUnformattedWhitespace(false);

			// see issue https://github.com/eclipse/xtext-core/issues/2058
			cfg.getRequest().setAllowIdentityEdits(true);

			// see issue https://github.com/eclipse/xtext-core/issues/164
			// and issue https://github.com/eclipse/xtext-core/issues/2060
			cfg.setUseSerializer(false);

			cfg.preferences(prefs -> {
				prefs.put(FormatterPreferenceKeys.indentation, "\t"); // Note: this should not be required if we have proper code formatting...
			});
		});
	}

	@Test
	void testFormatTypeAlias() {
		formatAndAssert("""
				namespace test


				typeAlias  maxNBoundedNumber
				 ( n int  ,max  number)
				     :
				   number ( digits  : n  ,  max: max )
				""", """
				namespace test

				typeAlias maxNBoundedNumber(n int, max number): number(digits: n, max: max)
				""");
	}

	@Test
	void testFormatTypeAliasWithDocumentation() {
		formatAndAssert("""
				namespace test


				typeAlias  maxNBoundedNumber
				 ( n int
				  ,max  number   <"The maximum bound on this number. If absent, this number is unbounded from above.">)
				     :  <"A bounded decimal number with N maximum number of digits.">
				   number ( digits  : n  ,  max: max )
				""", """
				namespace test

				typeAlias maxNBoundedNumber(
					n int
					, max number <"The maximum bound on this number. If absent, this number is unbounded from above.">
				): <"A bounded decimal number with N maximum number of digits.">
					number(digits: n, max: max)
				""");
	}

	@Test
	void testFormatRule() {
		formatAndAssert("""
				namespace drr.regulation.techsprint.g20.mas


				reporting rule OptionPremium from number: <"Option premium">
				    "N/A" as "I.6.3 Option Premium"
				""", """
				namespace drr.regulation.techsprint.g20.mas

				reporting rule OptionPremium from number: <"Option premium">
					"N/A"
						as "I.6.3 Option Premium"
				""");
	}

	@Test
	void testDocumentationEndsLine() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				func F:
					output:
						x int (1..1)

					set x: <"Docs"> 0
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				func F:
					output:
						x int (1..1)

					set x: <"Docs">
						0
				""");
	}

	@Test
	void testDocReference() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA
				corpus Regulation "600/2014" MiFIR\s
				corpus CommissionDelegatedRegulation "2017/590" RTS_22
				segment annex
				segment table
				segment field
				segment article

				reporting rule ReportStatus from number: <"Indication as to whether the transaction report is new or a cancellation">
					[regulatoryReference ESMA MiFIR RTS_22
							annex "I" table "2" field "1"
							provision "Indication as to whether the transaction report is new or a cancellation."]
					[regulatoryReference ESMA MiFIR RTS_22
							article "2"
							rationale_author "DRR"
							rationale "Article 2 means that each reportable event is treated as an independent transaction. The practical implementation of corrections is: A New event is a NEWT, A new, non-reportable version of a previously-reported event, that renders the previous version of the same event non-reportable, is a CANC, A new, non-reportable version of a non-reportable event is not reported, A new, reportable version of a previously-reported event, that supersedes the previous version of the same event is a CANC followed by a NEWT"
							structured_provision "MiFIR.ReportStatus is by definition 'NEWT' unless the report is a Cancellation when MiFIR.ReportStatus is by definition 'CANC'"
							provision "Indication as to whether the transaction report is new or a cancellation."]
					"Not Modelled"
						as "Report Status"
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA

				corpus Regulation "600/2014" MiFIR
				corpus CommissionDelegatedRegulation "2017/590" RTS_22

				segment annex
				segment table
				segment field
				segment article

				reporting rule ReportStatus from number: <"Indication as to whether the transaction report is new or a cancellation">
					[regulatoryReference ESMA MiFIR RTS_22 annex "I" table "2" field "1"
						provision "Indication as to whether the transaction report is new or a cancellation."]
					[regulatoryReference ESMA MiFIR RTS_22 article "2"
						rationale_author "DRR"
						rationale "Article 2 means that each reportable event is treated as an independent transaction. The practical implementation of corrections is: A New event is a NEWT, A new, non-reportable version of a previously-reported event, that renders the previous version of the same event non-reportable, is a CANC, A new, non-reportable version of a non-reportable event is not reported, A new, reportable version of a previously-reported event, that supersedes the previous version of the same event is a CANC followed by a NEWT"
						structured_provision "MiFIR.ReportStatus is by definition 'NEWT' unless the report is a Cancellation when MiFIR.ReportStatus is by definition 'CANC'"
						provision "Indication as to whether the transaction report is new or a cancellation."]
					"Not Modelled"
						as "Report Status"
				""");
	}

	@Test
	void testBuiltinFeaturesAreGrouped() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				basicType string
				basicType int

				library function DateRanges() date


				library function Min(x number, y number) number
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				basicType string
				basicType int

				library function DateRanges() date

				library function Min(x number, y number) number
				""");
	}

	@Test
	void testRedundantWhitespaceInEmptyRosettaFileIsRemoved() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"



				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"
				""");
	}

	@Test
	void simpleClassWithOneFieldIsFormatted() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
					version "test"
							type Test: <"Some definition"> field1 string (1..1) <"Field 1"> field2 string (1..1) <"Field 2">
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				type Test: <"Some definition">
					field1 string (1..1) <"Field 1">
					field2 string (1..1) <"Field 2">
				""");
	}

	@Test
	void stereotypeIsSurroundedWithSingleSpace() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model" version "test" type CalculationPeriod: <"xxx xxx.">
					 [synonym FpML value "CalculationPeriod"]

				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				type CalculationPeriod: <"xxx xxx.">
					[synonym FpML value "CalculationPeriod"]
				""");
	}

	@Test
	void synonymIsOnSingleIndentedLine() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"
				type CalculationPeriod : <"xxx xxx."> [synonym FpML value "CalculationPeriod"]
						// sinleline comment
									field1 string (1..1) <"Some Field">[synonym FpML value "CalculationPeriod"]

				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				type CalculationPeriod: <"xxx xxx.">
					[synonym FpML value "CalculationPeriod"]
					// sinleline comment
					field1 string (1..1) <"Some Field">
						[synonym FpML value "CalculationPeriod"]
				""");
	}

	@Test
	void conditionOnType() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"
				type CalculationPeriod : <"xxx xxx.">  [  metadata   scheme  ] [synonym FpML value "CalculationPeriod"]
									// sinleline comment
							field3 string (1..1) <"Some Field">  [  metadata   scheme  ]
							// sinleline comment
							[synonym FpML value "CalculationPeriod"] 					field1 string (1..1) <"Some Field">[synonym FpML value "CalculationPeriod"] condition: one-of condition Foo: field1
				condition Foo12: 	optional 		choice field1 , field3
				// sinleline comment
							condition Foo2:
				optional
				choice field1 , field3  condition Foo4:
				required
				choice field1 , field3
					condition:
					one-of
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				type CalculationPeriod: <"xxx xxx.">
					[metadata scheme]
					[synonym FpML value "CalculationPeriod"]
					// sinleline comment
					field3 string (1..1) <"Some Field">
						[metadata scheme]
						// sinleline comment
						[synonym FpML value "CalculationPeriod"]
					field1 string (1..1) <"Some Field">
						[synonym FpML value "CalculationPeriod"]

					condition:
						one-of

					condition Foo:
						field1

					condition Foo12:
						optional choice field1, field3

				// sinleline comment
					condition Foo2:
						optional choice field1, field3

					condition Foo4:
						required choice field1, field3

					condition:
						one-of
				""");
	}

	@Test
	void conditionOnFunc() {
		formatAndAssert("""
				namespace "test"
				version "test"
				type Type: foo string (1..1) func Execute2:[ metadata  scheme ] inputs:		product string (1..1) <"">[synonym FpML value "CalculationPeriod"] 		quantity string (1..1) 	output:
						execution Type (1..1) <""> condition Foo: product set execution -> foo:
						"sdf"set execution:
						execution
				set execution:
				execution
				post-condition:
				execution -> foo is absent
				""", """
				namespace "test"
				version "test"

				type Type:
					foo string (1..1)

				func Execute2:
					[metadata scheme]
					inputs:
						product string (1..1) <"">
							[synonym FpML value "CalculationPeriod"]
						quantity string (1..1)
					output:
						execution Type (1..1) <"">
					condition Foo:
						product
					set execution -> foo: "sdf"
					set execution: execution
					set execution: execution
					post-condition:
						execution -> foo is absent
				""");
	}

	@Test
	void formatDatesReferenceEnum() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model" version "test" enum DatesReferenceEnum: <"The enumerated values to specify the set of dates that can be referenced through FpML href constructs of type ...periodDatesReference.">
					tradeDate
					effectiveDate firstRegularPeriodStartDate lastRegularPeriodEndDate cashSettlementPaymentDate <"">
					calculationPeriodDates <"FpML business validation rule ird-59 specifies that if resetDates exists in resetDates/calculationPeriodDatesReference, the @href attribute is equal to the @id attribute of calculationPeriodDates in the same swapStream.">
					paymentDates resetDates fixingDates valuationDates
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				enum DatesReferenceEnum: <"The enumerated values to specify the set of dates that can be referenced through FpML href constructs of type ...periodDatesReference.">
					tradeDate
					effectiveDate
					firstRegularPeriodStartDate
					lastRegularPeriodEndDate
					cashSettlementPaymentDate <"">
					calculationPeriodDates <"FpML business validation rule ird-59 specifies that if resetDates exists in resetDates/calculationPeriodDatesReference, the @href attribute is equal to the @id attribute of calculationPeriodDates in the same swapStream.">
					paymentDates
					resetDates
					fixingDates
					valuationDates
				""");
	}

	@Test
	void formatDayOfWeekEnum() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model" version "test" enum DayOfWeekEnum: <"A day of the seven-day week."> [synonym FpML value "DayOfWeekEnum"] MON <"Monday">[synonym FpML value "MON"] TUE <"Tuesday">[synonym FpML value "TUE"] WED <"Wednesday">[synonym FpML value "WED"]THU <"Thursday">[synonym FpML value "THU"]FRI <"Friday">[synonym FpML value "FRI"]SAT <"Saturday">[synonym FpML value "SAT"]SUN <"Sunday">[synonym FpML value "SUN"]
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				enum DayOfWeekEnum: <"A day of the seven-day week.">
					[synonym FpML value "DayOfWeekEnum"]
					MON <"Monday">
						[synonym FpML value "MON"]
					TUE <"Tuesday">
						[synonym FpML value "TUE"]
					WED <"Wednesday">
						[synonym FpML value "WED"]
					THU <"Thursday">
						[synonym FpML value "THU"]
					FRI <"Friday">
						[synonym FpML value "FRI"]
					SAT <"Saturday">
						[synonym FpML value "SAT"]
					SUN <"Sunday">
						[synonym FpML value "SUN"]
				""");
	}

	@Test
	void formatSpreadScheduleTypeEnum() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"
				enum SpreadScheduleTypeEnum: <"The enumerated values to specify a long or short spread value."> 	[synonym FpML value "spreadScheduleTypeScheme"]
					Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
				[synonym FpML value "Long"] Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
						[synonym FpML value "Short"]
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				enum SpreadScheduleTypeEnum: <"The enumerated values to specify a long or short spread value.">
					[synonym FpML value "spreadScheduleTypeScheme"]
					Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
						[synonym FpML value "Long"]
					Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
						[synonym FpML value "Short"]
				""");
	}

	@Test
	void formatExternalSynomym() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				synonym source FpML_5_10

				synonym source MySynonymExtention extends FpML_5_10 {


					Contract:
					+ account	[value "my_account"]
					[value "my_account_2"]

					+ party [value "my_party"]


					Event:
						+ account
						[value "event_account"]



				}
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				synonym source FpML_5_10

				synonym source MySynonymExtention extends FpML_5_10
				{
					Contract:
						+ account
							[value "my_account"]
							[value "my_account_2"]
						+ party
							[value "my_party"]

					Event:
						+ account
							[value "event_account"]
				}
				""");
	}

	@Test
	void formatAttributeSynomym() {
		formatAndAssert("""
				namespace "test"
				version "test"
				synonym source SynSource

				type AllocationOutcome:
					allocatedTrade AllocationOutcome (1..*)
												[synonym SynSource value "originalTrade"]
					originalTrade string (1..1)<"">
						[synonym SynSource value "allocatedTrade"]

					condition AllocationOutcome_executionClosed: <"The allocation outcome must result in execution state of 'Allocated' for an execution.">
						if AllocationOutcome -> allocatedTrade  exists
						then allocatedTrade -> allocatedTrade -> allocatedTrade = allocatedTrade
					condition AllocationOutcome_contractClosed: <"The allocation outcome must result in a contract state of 'Allocated' for a contract.">
						if AllocationOutcome -> allocatedTrade  exists
						then allocatedTrade -> allocatedTrade -> allocatedTrade = allocatedTrade
					condition AllocationOutcome_contractClosed:
						one-of
				""", """
				namespace "test"
				version "test"

				synonym source SynSource

				type AllocationOutcome:
					allocatedTrade AllocationOutcome (1..*)
						[synonym SynSource value "originalTrade"]
					originalTrade string (1..1) <"">
						[synonym SynSource value "allocatedTrade"]

					condition AllocationOutcome_executionClosed: <"The allocation outcome must result in execution state of 'Allocated' for an execution.">
						if AllocationOutcome -> allocatedTrade exists
						then allocatedTrade -> allocatedTrade -> allocatedTrade = allocatedTrade

					condition AllocationOutcome_contractClosed: <"The allocation outcome must result in a contract state of 'Allocated' for a contract.">
						if AllocationOutcome -> allocatedTrade exists
						then allocatedTrade -> allocatedTrade -> allocatedTrade = allocatedTrade

					condition AllocationOutcome_contractClosed:
						one-of
				""");
	}

	@Test
	void formatIndentationTest() {
		formatterTestHelper.assertFormatted(cfg -> {
			cfg.preferences(prefs -> {
				prefs.put(FormatterPreferenceKeys.maxLineWidth, 20);
			});

			cfg.setExpectation("""
                     namespace "test"
        
                     type AllocationOutcome:
        
                         condition C1:
                             if True
                             then True
                                     extract [
                                         item = False
                                     ] = True
        
                         condition C2:
                             True
                     """);
			cfg.setToBeFormatted("""
					namespace "test"

					type AllocationOutcome:
						condition C1:
							if True
							then True extract [item = False] = True
						condition C2:
							True
					""");

			// extra check to make sure we didn't miss any hidden region in our formatter:
			cfg.setAllowUnformattedWhitespace(false);

			// see issue https://github.com/eclipse/xtext-core/issues/2058
			cfg.getRequest().setAllowIdentityEdits(true);

			// see issue https://github.com/eclipse/xtext-core/issues/164
			// and issue https://github.com/eclipse/xtext-core/issues/2060
			cfg.setUseSerializer(false);
		});
	}

	@Test
	void formatListLiteral() {
		formatAndAssert("""
				namespace "test"
							version "test"
				type Type:
								other Type (0..*)
				func Foo:
					inputs: in1 Type (1..1)
					output: out Type (1..1)
					add out -> other: [\s\s\s
					in1
					,in1
					,in1,
					in1
					]
				""", """
				namespace "test"
				version "test"

				type Type:
					other Type (0..*)

				func Foo:
					inputs:
						in1 Type (1..1)
					output:
						out Type (1..1)
					add out -> other: [in1, in1, in1, in1]
				""");
	}

	@Test
	void testChoice() {
		formatAndAssert("""
				namespace "test"
									version "test"
				choice Test:\s
				    A
				   \s
				    	B

				""", """
				namespace "test"
				version "test"

				choice Test:
					A
					B
				""");
	}
}
