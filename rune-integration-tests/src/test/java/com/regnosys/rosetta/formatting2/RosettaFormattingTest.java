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
			cfg.setExpectation(expectation);
			cfg.setToBeFormatted(unformatted);

			// extra check to make sure we didn't miss any hidden region in our formatter:
			cfg.setAllowUnformattedWhitespace(false);

			// see issue https://github.com/eclipse/xtext-core/issues/2058
			cfg.getRequest().setAllowIdentityEdits(true);

			// see issue https://github.com/eclipse/xtext-core/issues/164
			// and issue https://github.com/eclipse/xtext-core/issues/2060
			cfg.setUseSerializer(false);

			cfg.preferences(prefs -> {
                prefs.put(FormatterPreferenceKeys.lineSeparator, "\n");
				prefs.put(FormatterPreferenceKeys.indentation, "\t"); // Note: this should not be required if we have proper code formatting...
			});
		});
	}

	@Test
	void testCanHandleNonContiguousImportBlocks() {
		formatAndAssert("""
				namespace test

				import cdm.product.asset.*
				import cdm.product.common.settlement.*
				import cdm.product.template.*
				
				import fpml.consolidated.* as fpml
				import fpml.consolidated.asset.* as fpml
				import fpml.consolidated.dividend.swaps.* as fpml
				""", """
				namespace test

				import cdm.product.asset.*
				import cdm.product.common.settlement.*
				import cdm.product.template.*
				import fpml.consolidated.* as fpml
				import fpml.consolidated.asset.* as fpml
				import fpml.consolidated.dividend.swaps.* as fpml
				""");
	}

	@Test
	void testChoiceCommaSpacingWithMultipleAttributes() {
		formatAndAssert("""
				namespace test

				type Foo:
					field1 string (0..1)
					field2 string (0..1)
					field3 string (0..1)
					field4 string (0..1)

					condition Choice:
						required choice field1 , field2 , field3 , field4
				""", """
				namespace test
				
				type Foo:
					field1 string (0..1)
					field2 string (0..1)
					field3 string (0..1)
					field4 string (0..1)
				
					condition Choice:
						required choice field1, field2, field3, field4
				""");
	}

	@Test
    void testFormatFunctionExtension() {
        formatAndAssert("""
                namespace test
                scope MyScope
                
                func Foo extends Bar:
                    output:
                        result int (1..1)
                    set result: super(123, "foo")
                """, """
                namespace test
                scope MyScope
                
                func Foo extends Bar:
                	output:
                		result int (1..1)
                	set result: super(123, "foo")
                """);
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


				reporting rule OptionPremium from number: <"Option premium"> "N/A"
				""", """
				namespace drr.regulation.techsprint.g20.mas

				reporting rule OptionPremium from number: <"Option premium">
					"N/A"
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
					[docReference ESMA MiFIR RTS_22
							annex "I" table "2" field "1"
							provision "Indication as to whether the transaction report is new or a cancellation."]
					[docReference ESMA MiFIR RTS_22
							article "2"
							rationale_author "DRR"
							rationale "Article 2 means that each reportable event is treated as an independent transaction. The practical implementation of corrections is: A New event is a NEWT, A new, non-reportable version of a previously-reported event, that renders the previous version of the same event non-reportable, is a CANC, A new, non-reportable version of a non-reportable event is not reported, A new, reportable version of a previously-reported event, that supersedes the previous version of the same event is a CANC followed by a NEWT"
							structured_provision "MiFIR.ReportStatus is by definition 'NEWT' unless the report is a Cancellation when MiFIR.ReportStatus is by definition 'CANC'"
							provision "Indication as to whether the transaction report is new or a cancellation."]
					"Not Modelled"
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
					[docReference ESMA MiFIR RTS_22 annex "I" table "2" field "1"
						provision "Indication as to whether the transaction report is new or a cancellation."]
					[docReference ESMA MiFIR RTS_22 article "2"
						rationale_author "DRR"
						rationale "Article 2 means that each reportable event is treated as an independent transaction. The practical implementation of corrections is: A New event is a NEWT, A new, non-reportable version of a previously-reported event, that renders the previous version of the same event non-reportable, is a CANC, A new, non-reportable version of a non-reportable event is not reported, A new, reportable version of a previously-reported event, that supersedes the previous version of the same event is a CANC followed by a NEWT"
						structured_provision "MiFIR.ReportStatus is by definition 'NEWT' unless the report is a Cancellation when MiFIR.ReportStatus is by definition 'CANC'"
						provision "Indication as to whether the transaction report is new or a cancellation."]
					"Not Modelled"
				""");
	}

	@Test
	void testDocReferenceOnType() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA
				corpus Regulation "600/2014" MiFIR
				corpus CommissionDelegatedRegulation "2017/590" RTS_22
				segment annex
				segment article

				type Product: [docReference ESMA MiFIR RTS_22 article "2" provision "Transaction report details."] [docReference ESMA MiFIR RTS_22 annex "I"]
					id string (1..1)
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA

				corpus Regulation "600/2014" MiFIR
				corpus CommissionDelegatedRegulation "2017/590" RTS_22

				segment annex
				segment article

				type Product:
					[docReference ESMA MiFIR RTS_22 article "2"
						provision "Transaction report details."]
					[docReference ESMA MiFIR RTS_22 annex "I"]
					id string (1..1)
				""");
	}

	@Test
	void testDocReferenceOnEnumAndEnumValue() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA
				corpus Regulation "600/2014" MiFIR
				segment annex
				segment article

				enum Color: <"Some colors."> [docReference ESMA MiFIR article "1" provision "Color enum."]
					RED <"Red color."> [docReference ESMA MiFIR article "2" provision "Red value."]
					GREEN [docReference ESMA MiFIR article "3" provision "Green value."] [docReference ESMA MiFIR annex "I"]
					BLUE
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				body Authority ESMA

				corpus Regulation "600/2014" MiFIR

				segment annex
				segment article

				enum Color: <"Some colors.">
					[docReference ESMA MiFIR article "1"
						provision "Color enum."]
					RED <"Red color.">
						[docReference ESMA MiFIR article "2"
							provision "Red value."]
					GREEN
						[docReference ESMA MiFIR article "3"
							provision "Green value."]
						[docReference ESMA MiFIR annex "I"]
					BLUE
				""");
	}

	@Test
	void testImports() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test" import com.regnosys.rosetta.common.* import com.regnosys.rosetta.product.* as product
				type Product:
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				import com.regnosys.rosetta.common.*
				import com.regnosys.rosetta.product.* as product

				type Product:
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
	void conditionOnType() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"
				type CalculationPeriod : <"xxx xxx.">  [  metadata   scheme  ]
									// sinleline comment
							field3 string (1..1) <"Some Field">  [  metadata   scheme  ]
							// sinleline comment
											field1 string (1..1) <"Some Field"> condition: one-of condition Foo: field1
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
					// sinleline comment
					field3 string (1..1) <"Some Field">
						[metadata scheme]
					// sinleline comment
					field1 string (1..1) <"Some Field">
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
				type Type: foo string (1..1) func Execute2:[ metadata  scheme ] inputs:		product string (1..1) <""> 		quantity string (1..1) 	output:
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
				namespace "com.regnosys.rosetta.model" version "test" enum DayOfWeekEnum: <"A day of the seven-day week."> MON <"Monday"> TUE <"Tuesday"> WED <"Wednesday">THU <"Thursday">FRI <"Friday">SAT <"Saturday">SUN <"Sunday">
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				enum DayOfWeekEnum: <"A day of the seven-day week.">
					MON <"Monday">
					TUE <"Tuesday">
					WED <"Wednesday">
					THU <"Thursday">
					FRI <"Friday">
					SAT <"Saturday">
					SUN <"Sunday">
				""");
	}

	@Test
	void formatSpreadScheduleTypeEnum() {
		formatAndAssert("""
				namespace "com.regnosys.rosetta.model"
				version "test"
				enum SpreadScheduleTypeEnum: <"The enumerated values to specify a long or short spread value.">
					Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
				Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
				""", """
				namespace "com.regnosys.rosetta.model"
				version "test"

				enum SpreadScheduleTypeEnum: <"The enumerated values to specify a long or short spread value.">
					Long <"Represents a Long Spread Schedule. Spread schedules defined as 'Long' will be applied to Long Positions.">
					Short <"Represents a Short Spread Schedule. Spread schedules defined as 'Short' will be applied to Short Positions.">
				""");
	}

	@Test
	void formatIndentationTest() {
		formatterTestHelper.assertFormatted(cfg -> {
			cfg.preferences(prefs -> {
                prefs.put(FormatterPreferenceKeys.lineSeparator, "\n");
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
