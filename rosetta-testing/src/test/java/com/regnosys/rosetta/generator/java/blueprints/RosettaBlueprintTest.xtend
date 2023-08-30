package com.regnosys.rosetta.generator.java.blueprints

import com.google.inject.Guice
import com.google.inject.Injector
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock
import org.eclipse.xtext.diagnostics.Diagnostic
import javax.inject.Inject

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
//@Disabled("This test fails after the 2.15 xtext upgrade because the generated code does not compile with the eclipse compiler using mvn.")
class RosettaBlueprintTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

	@Test
	def void parseSimpleRule() {
		val r = '''
			eligibility rule ReportableTransation from ReportableEvent:
				[legacy-syntax]
				return "y"
		'''
		parseRosettaWithNoErrors(r)
	}
	
	@Test
	def void legacyRuleCanCallCoreSyntaxRule() {
		val r = '''
			type T:
				attr number (1..1)
			
			reporting rule Foo from T:
				[legacy-syntax]
				extract T -> attr * 2
				then Bar
			
			reporting rule Bar from number:
				item / 2
		'''
		parseRosettaWithNoIssues(r)
	}

	static final CharSequence REPORT_TYPES = '''
					namespace com.rosetta.test.model

					type Bar:
						bar1 string (0..1)
						bar2 string (0..*)
						baz Baz (1..1)
						quxList Qux (0..*)

					type Baz:
						 baz1 string (1..1)

					type Qux:
						 qux1 string (1..1)
						 qux2 string (1..1)

					type Quux:
						quux1 string (1..1)
						quux2 string (1..1)

				'''

	static final CharSequence REPORT_RULES = '''
					namespace com.rosetta.test.model

					eligibility rule FooRule from Bar:
						[legacy-syntax]
						filter when Bar->bar1 exists

					reporting rule BarBarOne from Bar:
						[legacy-syntax]
						extract Bar->bar1 as "1 BarOne"

					reporting rule BarBarTwo from Bar:
						[legacy-syntax]
						extract Bar->bar2 as "2 BarTwo"

					reporting rule BarBaz from Bar:
						[legacy-syntax]
						extract Bar->baz->baz1 as "3 BarBaz"

					reporting rule BarQuxList from Bar:
						[legacy-syntax]
						extract repeatable Bar->quxList then
						(
							QuxQux1,
							QuxQux2
						) as "4 BarQuxList"

					reporting rule QuxQux1 from Qux:
						[legacy-syntax]
						extract Qux->qux1 as "5 QuxQux1"

					reporting rule QuxQux2 from Qux:
						[legacy-syntax]
						extract Qux->qux2 as "6 QuxQux2"

					reporting rule BarQuux from Bar:
						[legacy-syntax]
						extract Create_Quux( Bar->baz ) as "7 BarQuux"

					func Create_Quux:
						inputs:
							baz Baz (1..1)
						output:
							quux Quux (1..1)

						set quux -> quux1: baz -> baz1
						set quux -> quux2: baz -> baz1

				'''



	@Test
	def void parseSimpleReportForTypeWithInlineRuleReferences() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR

					report TEST_REG MiFIR in T+1
					from Bar
					when FooRule
					with type BarReport

					type BarReport:
						barBarOne string (1..1)
							[ruleReference BarBarOne]
						barBarTwo string (1..1)
							[ruleReference BarBarTwo]
						barBaz BarBazReport (1..1)
						barQuxList BarQuxReport (0..*)
							[ruleReference BarQuxList]
						barQuux Quux (1..1)
							[ruleReference BarQuux]

					type BarBazReport:
						barBaz1 string (1..1)
							[ruleReference BarBaz]

					type BarQuxReport:
						bazQux1 string (1..1)
							[ruleReference QuxQux1]
						bazQux2 string (1..1)
							[ruleReference QuxQux2]

				''']
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.mapper.MapperC;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.BarQuxReport;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.BarReport.BarReportBuilder;
			import java.util.Optional;
			import javax.inject.Inject;
			
			
			@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
			public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected BarBarOneRule barBarOne;
				@Inject protected BarBarTwoRule barBarTwo;
				@Inject protected BarBazRule barBaz;
				@Inject protected BarQuuxRule barQuux;
				@Inject protected BarQuxListRule barQuxList;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public BarReport evaluate(Bar input) {
					BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
					
					final BarReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(BarReport.class, output);
					}
					
					return output;
				}
			
				protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
			
				public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
					@Override
					protected BarReport.BarReportBuilder doEvaluate(Bar input) {
						BarReport.BarReportBuilder output = BarReport.builder();
						return assignOutput(output, input);
					}
					
					protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
						output
							.setBarBarOne(MapperS.of(barBarOne.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).get());
						
						output
							.getOrCreateBarBaz()
							.setBarBaz1(MapperS.of(barBaz.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarQuxList(MapperC.<BarQuxReport>of(barQuxList.evaluate(MapperS.of(input).get())).getMulti());
						
						output
							.setBarQuux(MapperS.of(barQuux.evaluate(MapperS.of(input).get())).get());
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
			'''
			assertEquals(expected, reportJava)

		} finally {
		}
		val reportBuilderJava = code.get("com.rosetta.test.model.blueprint.BarReport_DataItemReportBuilder")
		try {
			assertThat(reportBuilderJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
			import com.regnosys.rosetta.blueprints.DataItemReportUtils;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.Quux;
			import java.util.ArrayList;
			import java.util.Collection;
			import java.util.List;
			import java.util.stream.Collectors;
			
			
			/**
			 * @version 0.0.0
			 */
			public class BarReport_DataItemReportBuilder implements DataItemReportBuilder {
			
				@Override
				public <T> BarReport buildReport(Collection<GroupableData<?, T>> reportData) {
					BarReport.BarReportBuilder dataItemReportBuilder = BarReport.builder();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
							}
							if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
							}
							if (BarBazRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
							}
							if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
							}
							if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
							}
							if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
							}
						}
					}
					
					return dataItemReportBuilder.build();
				}
				
				@Override
				public <T> List<BarReport> buildReportList(Collection<GroupableData<?, T>> reportData) {
					List<BarReport.BarReportBuilder> listBuilder = new ArrayList();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							int index = ruleIdentifier.getRepeatableIndex().orElse(0);
							while (index >= listBuilder.size()) {
								listBuilder.add(null);
							}
							BarReport.BarReportBuilder dataItemReportBuilder = listBuilder.get(index);
							if (dataItemReportBuilder == null) {
								dataItemReportBuilder = BarReport.builder();
								listBuilder.set(index, dataItemReportBuilder);
							}
							if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
							}
							if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
							}
							if (BarBazRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
							}
							if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
							}
							if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
							}
							if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
							}
						}
					}
					
					return listBuilder.stream()
						.map((item) -> {
							if (item != null) {
								return item.build();
							}
							return null;
						})
						.collect(Collectors.toList());
				}
			}
			'''
			assertEquals(expected, reportBuilderJava)

		} finally {
		}
		code.compileToClasses
	}

	@Test
	def void parseSimpleReportForTypeWithExternalRuleReferences() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR

					report TEST_REG MiFIR in T+1
					from Bar
					when FooRule
					with type BarReport
					with source RuleSource

					type BarReport:
						barBarOne string (1..1)
						barBarTwo string (1..1)
						barBaz BarBazReport (1..1)
						barQuxList BarQuxReport (0..*)
						barQuux Quux (1..1)

					type BarBazReport:
						barBaz1 string (1..1)

					type BarQuxReport:
						bazQux1 string (1..1)
						bazQux2 string (1..1)

					rule source RuleSource {

						BarReport:
							+ barBarOne
								[ruleReference BarBarOne]
							+ barBarTwo
								[ruleReference BarBarTwo]
							+ barQuxList
								[ruleReference BarQuxList]
							+ barQuux
								[ruleReference BarQuux]

						BarBazReport:
							+ barBaz1
								[ruleReference BarBaz]

						BarQuxReport:
							+ bazQux1
								[ruleReference QuxQux1]
							+ bazQux2
								[ruleReference QuxQux2]
					}
				''']
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.mapper.MapperC;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.BarQuxReport;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.BarReport.BarReportBuilder;
			import java.util.Optional;
			import javax.inject.Inject;
			
			
			@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
			public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected BarBarOneRule barBarOne;
				@Inject protected BarBarTwoRule barBarTwo;
				@Inject protected BarBazRule barBaz;
				@Inject protected BarQuuxRule barQuux;
				@Inject protected BarQuxListRule barQuxList;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public BarReport evaluate(Bar input) {
					BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
					
					final BarReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(BarReport.class, output);
					}
					
					return output;
				}
			
				protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
			
				public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
					@Override
					protected BarReport.BarReportBuilder doEvaluate(Bar input) {
						BarReport.BarReportBuilder output = BarReport.builder();
						return assignOutput(output, input);
					}
					
					protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
						output
							.setBarBarOne(MapperS.of(barBarOne.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).get());
						
						output
							.getOrCreateBarBaz()
							.setBarBaz1(MapperS.of(barBaz.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarQuxList(MapperC.<BarQuxReport>of(barQuxList.evaluate(MapperS.of(input).get())).getMulti());
						
						output
							.setBarQuux(MapperS.of(barQuux.evaluate(MapperS.of(input).get())).get());
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
			'''
			assertEquals(expected, reportJava)

		} finally {
		}
		val reportBuilderJava = code.get("com.rosetta.test.model.blueprint.BarReport_DataItemReportBuilder")
		try {
			assertThat(reportBuilderJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
			import com.regnosys.rosetta.blueprints.DataItemReportUtils;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.Quux;
			import java.util.ArrayList;
			import java.util.Collection;
			import java.util.List;
			import java.util.stream.Collectors;
			
			
			/**
			 * @version 0.0.0
			 */
			public class BarReport_DataItemReportBuilder implements DataItemReportBuilder {
			
				@Override
				public <T> BarReport buildReport(Collection<GroupableData<?, T>> reportData) {
					BarReport.BarReportBuilder dataItemReportBuilder = BarReport.builder();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
							}
							if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
							}
							if (BarBazRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
							}
							if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
							}
							if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
							}
							if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
							}
						}
					}
					
					return dataItemReportBuilder.build();
				}
				
				@Override
				public <T> List<BarReport> buildReportList(Collection<GroupableData<?, T>> reportData) {
					List<BarReport.BarReportBuilder> listBuilder = new ArrayList();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							int index = ruleIdentifier.getRepeatableIndex().orElse(0);
							while (index >= listBuilder.size()) {
								listBuilder.add(null);
							}
							BarReport.BarReportBuilder dataItemReportBuilder = listBuilder.get(index);
							if (dataItemReportBuilder == null) {
								dataItemReportBuilder = BarReport.builder();
								listBuilder.set(index, dataItemReportBuilder);
							}
							if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
							}
							if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
							}
							if (BarBazRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
							}
							if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
							}
							if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
							}
							if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
							}
						}
					}
					
					return listBuilder.stream()
						.map((item) -> {
							if (item != null) {
								return item.build();
							}
							return null;
						})
						.collect(Collectors.toList());
				}
			}
			'''
			assertEquals(expected, reportBuilderJava)

		} finally {
		}
		code.compileToClasses
	}

	@Test
	def void parseSimpleReportForTypeWithExternalRuleReferences_OverrideAdd() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR

					report TEST_REG MiFIR in T+1
					from Bar
					when FooRule
					with type BarReport
					with source RuleSource

					type BarReport:
						barBarOne string (1..1)
							[ruleReference BarBarOne]
						barBarTwo string (1..1)
							[ruleReference BarBarTwo]
						barBaz BarBazReport (1..1)
						barQuxList BarQuxReport (0..*)
							[ruleReference BarQuxList]

					type BarBazReport:
						barBaz1 string (1..1)
							[ruleReference BarBaz]

					type BarQuxReport:
						bazQux1 string (1..1)
							[ruleReference QuxQux1]
						bazQux2 string (1..1)
							[ruleReference QuxQux2]

					reporting rule New_BarBarOne from Bar:
						[legacy-syntax]
						extract Bar->bar1 + "NEW" as "1 New BarOne"

					rule source RuleSource {

						BarReport:
							- barBarOne
							+ barBarOne
								[ruleReference New_BarBarOne]
					}
				''']
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.mapper.MapperC;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.BarQuxReport;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.BarReport.BarReportBuilder;
			import java.util.Optional;
			import javax.inject.Inject;
			
			
			@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
			public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected BarBarTwoRule barBarTwo;
				@Inject protected BarBazRule barBaz;
				@Inject protected BarQuxListRule barQuxList;
				@Inject protected New_BarBarOneRule new_BarBarOne;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public BarReport evaluate(Bar input) {
					BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
					
					final BarReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(BarReport.class, output);
					}
					
					return output;
				}
			
				protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
			
				public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
					@Override
					protected BarReport.BarReportBuilder doEvaluate(Bar input) {
						BarReport.BarReportBuilder output = BarReport.builder();
						return assignOutput(output, input);
					}
					
					protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
						output
							.setBarBarOne(MapperS.of(new_BarBarOne.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).get());
						
						output
							.getOrCreateBarBaz()
							.setBarBaz1(MapperS.of(barBaz.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarQuxList(MapperC.<BarQuxReport>of(barQuxList.evaluate(MapperS.of(input).get())).getMulti());
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
			'''
			assertEquals(expected, reportJava)

		} finally {
		}
		code.compileToClasses
	}

	@Test
	def void parseSimpleReportForTypeWithExternalRuleReferences_OverrideRemove() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR

					report TEST_REG MiFIR in T+1
					from Bar
					when FooRule
					with type BarReport
					with source RuleSource

					type BarReport:
						barBarOne string (1..1)
							[ruleReference BarBarOne]
						barBarTwo string (1..1)
							[ruleReference BarBarTwo]
						barBaz BarBazReport (1..1)
						barQuxList BarQuxReport (0..*)
							[ruleReference BarQuxList]

					type BarBazReport:
						barBaz1 string (1..1)
							[ruleReference BarBaz]

					type BarQuxReport:
						bazQux1 string (1..1)
							[ruleReference QuxQux1]
						bazQux2 string (1..1)
							[ruleReference QuxQux2]

					rule source RuleSource {

						BarReport:
							- barBarOne
					}
				''']
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.mapper.MapperC;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.BarQuxReport;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.BarReport.BarReportBuilder;
			import java.util.Optional;
			import javax.inject.Inject;
			
			
			@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
			public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected BarBarTwoRule barBarTwo;
				@Inject protected BarBazRule barBaz;
				@Inject protected BarQuxListRule barQuxList;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public BarReport evaluate(Bar input) {
					BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
					
					final BarReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(BarReport.class, output);
					}
					
					return output;
				}
			
				protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
			
				public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
					@Override
					protected BarReport.BarReportBuilder doEvaluate(Bar input) {
						BarReport.BarReportBuilder output = BarReport.builder();
						return assignOutput(output, input);
					}
					
					protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
						output
							.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).get());
						
						output
							.getOrCreateBarBaz()
							.setBarBaz1(MapperS.of(barBaz.evaluate(MapperS.of(input).get())).get());
						
						output
							.setBarQuxList(MapperC.<BarQuxReport>of(barQuxList.evaluate(MapperS.of(input).get())).getMulti());
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
			'''
			assertEquals(expected, reportJava)

		} finally {
		}
		code.compileToClasses
	}

	@Test
	def void parseSimpleReportWithDifferentNS() {
		val model = #['''
			namespace ns1
			
			type Bar:
				bar1 string (0..1)
				bar2 string (1..1)
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule BarBarTwo from Bar:
				[legacy-syntax]
				extract Bar->bar2 as "2 BarTwo"
			
		''','''
			namespace ns3
			
			import ns1.*
			import ns2.*
			
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
				from Bar
				when FooRule
				with type BarReport
			
			eligibility rule FooRule from Bar:
				[legacy-syntax]
				filter when Bar->bar1 exists
			
			reporting rule BarBarOne from Bar:
				[legacy-syntax]
				extract Bar->bar1 as "1 BarOne"
			
			type BarReport:
				barBarOne string (1..1)
					[ruleReference BarBarOne]
				barBarTwo string (1..1)
					[ruleReference BarBarTwo]
			
		'''
		]
		val code = model.generateCode
		//println(code)
		val reportBuilderJava = code.get("ns3.blueprint.BarReport_DataItemReportBuilder")
		try {
			assertThat(reportBuilderJava, CoreMatchers.notNullValue())
			val expected = '''
				package ns3.blueprint;
				
				import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
				import com.regnosys.rosetta.blueprints.DataItemReportUtils;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				import java.util.stream.Collectors;
				import ns2.blueprint.BarBarTwoRule;
				import ns3.BarReport;
				
				
				/**
				 * @version 0.0.0
				 */
				public class BarReport_DataItemReportBuilder implements DataItemReportBuilder {
				
					@Override
					public <T> BarReport buildReport(Collection<GroupableData<?, T>> reportData) {
						BarReport.BarReportBuilder dataItemReportBuilder = BarReport.builder();
						
						for (GroupableData<?, T> groupableData : reportData) {
							DataIdentifier dataIdentifier = groupableData.getIdentifier();
							if (dataIdentifier instanceof RuleIdentifier) {
								RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
								Class<?> ruleType = ruleIdentifier.getRuleType();
								Object data = groupableData.getData();
								if (data == null) {
									continue;
								}
								if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
							}
						}
						
						return dataItemReportBuilder.build();
					}
					
					@Override
					public <T> List<BarReport> buildReportList(Collection<GroupableData<?, T>> reportData) {
						List<BarReport.BarReportBuilder> listBuilder = new ArrayList();
						
						for (GroupableData<?, T> groupableData : reportData) {
							DataIdentifier dataIdentifier = groupableData.getIdentifier();
							if (dataIdentifier instanceof RuleIdentifier) {
								RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
								Class<?> ruleType = ruleIdentifier.getRuleType();
								Object data = groupableData.getData();
								if (data == null) {
									continue;
								}
								int index = ruleIdentifier.getRepeatableIndex().orElse(0);
								while (index >= listBuilder.size()) {
									listBuilder.add(null);
								}
								BarReport.BarReportBuilder dataItemReportBuilder = listBuilder.get(index);
								if (dataItemReportBuilder == null) {
									dataItemReportBuilder = BarReport.builder();
									listBuilder.set(index, dataItemReportBuilder);
								}
								if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
							}
						}
						
						return listBuilder.stream()
							.map((item) -> {
								if (item != null) {
									return item.build();
								}
								return null;
							})
							.collect(Collectors.toList());
					}
				}
			'''
			assertEquals(expected, reportBuilderJava)

		} finally {
		}
		code.compileToClasses
	}
	
	@Test
	def void parseSimpleReportWithEmptyType() {
		val model = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			from Bar
			when FooRule
			with type BarReport
			
			eligibility rule FooRule from Bar:
				[legacy-syntax]
				filter when Bar->bar1 exists
			
			type BarReport:
				barBarOne string (1..1)
					// no rules
			
			type Bar:
				bar1 string (0..1)

		'''
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.BarReport;
			import com.rosetta.test.model.BarReport.BarReportBuilder;
			import java.util.Optional;
			import javax.inject.Inject;
			
			
			@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
			public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public BarReport evaluate(Bar input) {
					BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
					
					final BarReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(BarReport.class, output);
					}
					
					return output;
				}
			
				protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
			
				public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
					@Override
					protected BarReport.BarReportBuilder doEvaluate(Bar input) {
						BarReport.BarReportBuilder output = BarReport.builder();
						return assignOutput(output, input);
					}
					
					protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
			'''
			assertEquals(expected, reportJava)

		} finally {
		}
		val reportBuilderJava = code.get("com.rosetta.test.model.blueprint.BarReport_DataItemReportBuilder")
		try {
			assertThat(reportBuilderJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.test.model.BarReport;
			import java.util.ArrayList;
			import java.util.Collection;
			import java.util.List;
			import java.util.stream.Collectors;
			
			
			/**
			 * @version test
			 */
			public class BarReport_DataItemReportBuilder implements DataItemReportBuilder {
			
				@Override
				public <T> BarReport buildReport(Collection<GroupableData<?, T>> reportData) {
					BarReport.BarReportBuilder dataItemReportBuilder = BarReport.builder();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
						}
					}
					
					return dataItemReportBuilder.build();
				}
				
				@Override
				public <T> List<BarReport> buildReportList(Collection<GroupableData<?, T>> reportData) {
					List<BarReport.BarReportBuilder> listBuilder = new ArrayList();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							int index = ruleIdentifier.getRepeatableIndex().orElse(0);
							while (index >= listBuilder.size()) {
								listBuilder.add(null);
							}
							BarReport.BarReportBuilder dataItemReportBuilder = listBuilder.get(index);
							if (dataItemReportBuilder == null) {
								dataItemReportBuilder = BarReport.builder();
								listBuilder.set(index, dataItemReportBuilder);
							}
						}
					}
					
					return listBuilder.stream()
						.map((item) -> {
							if (item != null) {
								return item.build();
							}
							return null;
						})
						.collect(Collectors.toList());
				}
			}
			'''
			assertEquals(expected, reportBuilderJava)

		} finally {
		}
		code.compileToClasses
	}
	
	@Test
	def void parseReportWithType() {
		val model = '''
			namespace "test.reg"
			version "test"
			
			body Authority Shield <"Strategic Homeland Intervention, Enforcement and Logistics Division">
			
			corpus Act "Avengers Initiative" Avengers <"The Avengers Initiative (a.k.a Phase 1; originally conceptualized as the Protector Initiative) was a secret project created by S.H.I.E.L.D. to create the Avengers, a response team comprised of the most able individuals humankind has to offer. The Initiative will defend Earth from imminent global threats that are beyond the warfighting capability of conventional military forces. ">
			
			corpus Regulations "Sokovia Accords" SokoviaAccords <"The Sokovia Accords are a set of legal documents designed to regulate the activities of enhanced individuals, specifically those who work for either government agencies such as S.H.I.E.L.D. or for private organizations such as the Avengers">
			
			segment rationale
			segment rationale_author
			segment structured_provision
			
			segment section
			segment field
			
			report Shield Avengers SokoviaAccords in real-time
				from Person
			    when HasSuperPowers
			    with type SokoviaAccordsReport
			
			type SokoviaAccordsReport:
			    heroName string (1..1) <"Basic type - string">
			        [ruleReference HeroName]
			    dateOfBirth date (1..1) <"Basic type - date">
			        [ruleReference DateOfBirth]
			    nationality CountryEnum (1..1) <"Enum type">
			        [ruleReference Nationality]
			    hasSpecialAbilities boolean (1..1) <"Basic type - boolean">
			        [ruleReference SpecialAbilities]
			    powers PowerEnum (0..1) <"Enum type - multiple cardinality not allowed">
			        [ruleReference Powers]
			    attribute AttributeReport (0..1)  <"Nested report">
			    organisations OrganisationReport (0..*) <"Repeatable rule">
			        [ruleReference HeroOrganisations]
			    notModelled string (1..1) <"Not modelled">
			        [ruleReference NotModelled]
			
			type AttributeReport:
			    heroInt int (1..1) <"Basic type - int">
			        [ruleReference AttributeInt]
			    heroNumber number (1..1) <"Basic type - number">
			        [ruleReference AttributeNumber]
			    heroTime time (1..1) <"Basic type - time">
			        [ruleReference AttributeTime]
			    heroZonedDateTime zonedDateTime (1..1) <"Record type - zonedDateTime">
			        [ruleReference AttributeZonedDateTime]
			
			type OrganisationReport: <"Repeated rule">
			    name string (1..1)
			        [ruleReference OrganisationName]
			    isGovernmentAgency boolean (1..1)
			        [ruleReference IsGovernmentAgency]
			    country CountryEnum (1..1)
			        [ruleReference OrganisationCountry]
			
			eligibility rule HasSuperPowers from Person:
				[legacy-syntax]
			    filter when Person -> hasSpecialAbilities
			
			reporting rule HeroName from Person: <"Name">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "1" provision "Hero Name."]
			    extract Person -> name as "Hero Name"
			
			reporting rule DateOfBirth from Person: <"Date of birth">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Date of birth."]
			    extract Person -> dateOfBirth as "Date of Birth"
			
			reporting rule Nationality from Person: <"Nationality">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Nationality."]
			    extract Person -> nationality as "Nationality"
			
			reporting rule SpecialAbilities from Person: <"Has Special Abilities">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "3" provision "Has Special Abilities"]
			    extract Person -> hasSpecialAbilities as "Has Special Abilities"
			
			reporting rule Powers from Person: <"Super Power Name">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Powers."]
			    extract Person -> powers as "Powers"
			
			reporting rule AttributeInt from Person: <"Attribute - Int">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Int."]
			    extract Person -> attribute -> heroInt as "Attribute - Int"
			
			reporting rule AttributeNumber from Person: <"Attribute - Number">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Number."]
			    extract Person -> attribute -> heroNumber as "Attribute - Number"
			
			reporting rule AttributeZonedDateTime from Person: <"Attribute - ZonedDateTime">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - ZonedDateTime."]
			    extract Person -> attribute -> heroZonedDateTime as "Attribute - ZonedDateTime"
			
			reporting rule AttributeTime from Person: <"Attribute - Time">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Time."]
			    extract Person -> attribute -> heroTime as "Attribute - Time"
			
			reporting rule HeroOrganisations from Person: <"Has Special Abilities">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract repeatable Person -> organisations then
			    (
			        OrganisationName,
			        OrganisationCountry,
			        IsGovernmentAgency
			    ) as "Hero Organisations"
			
			reporting rule OrganisationName from Organisation: <"Has Special Abilities">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> name as "Organisation Name"
			
			reporting rule OrganisationCountry from Organisation: <"Has Special Abilities">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> country as "Organisation Country"
			
			reporting rule IsGovernmentAgency from Organisation: <"Has Special Abilities">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> isGovernmentAgency as "Is Government Agency"
			
			reporting rule NotModelled from Person: <"Not Modelled">
				[legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "6"  provision "Not Modelled."]
			    return "Not modelled" as "Not Modelled"
			
			type Person:
			    name string (1..1)
			    dateOfBirth date (1..1)
			    nationality CountryEnum (1..1)
			    hasSpecialAbilities boolean (1..1)
			    powers PowerEnum (0..*)
			    attribute Attribute (0..1)
			    organisations Organisation (0..*)
			
			type Attribute:
			    heroInt int (1..1)
			    heroNumber number (1..1)
			    heroZonedDateTime zonedDateTime (1..1)
			    heroTime time (1..1)
			
			type Organisation:
			    name string (1..1)
			    isGovernmentAgency boolean (1..1)
			    country CountryEnum (1..1)
			
			enum PowerEnum:
			    Armour
			    Flight
			    SuperhumanReflexes
			    SuperhumanStrength
			
			enum CountryEnum:
			    UnitedStatesOfAmerica
		'''
		val code = model.generateCode
		val reportBuilderJava = code.get("test.reg.blueprint.SokoviaAccordsReport_DataItemReportBuilder")
		try {
			assertThat(reportBuilderJava, CoreMatchers.notNullValue())
			val expected = '''
			package test.reg.blueprint;
			
			import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
			import com.regnosys.rosetta.blueprints.DataItemReportUtils;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.model.lib.records.Date;
			import java.math.BigDecimal;
			import java.time.LocalTime;
			import java.time.ZonedDateTime;
			import java.util.ArrayList;
			import java.util.Collection;
			import java.util.List;
			import java.util.stream.Collectors;
			import test.reg.CountryEnum;
			import test.reg.PowerEnum;
			import test.reg.SokoviaAccordsReport;
			
			
			/**
			 * @version test
			 */
			public class SokoviaAccordsReport_DataItemReportBuilder implements DataItemReportBuilder {
			
				@Override
				public <T> SokoviaAccordsReport buildReport(Collection<GroupableData<?, T>> reportData) {
					SokoviaAccordsReport.SokoviaAccordsReportBuilder dataItemReportBuilder = SokoviaAccordsReport.builder();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							if (AttributeIntRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroInt, Integer.class, data, AttributeIntRule.class);
							}
							if (AttributeNumberRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroNumber, BigDecimal.class, data, AttributeNumberRule.class);
							}
							if (AttributeTimeRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroTime, LocalTime.class, data, AttributeTimeRule.class);
							}
							if (AttributeZonedDateTimeRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroZonedDateTime, ZonedDateTime.class, data, AttributeZonedDateTimeRule.class);
							}
							if (DateOfBirthRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setDateOfBirth, Date.class, data, DateOfBirthRule.class);
							}
							if (HeroNameRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setHeroName, String.class, data, HeroNameRule.class);
							}
							if (IsGovernmentAgencyRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setIsGovernmentAgency, Boolean.class, data, IsGovernmentAgencyRule.class);
							}
							if (NationalityRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setNationality, CountryEnum.class, data, NationalityRule.class);
							}
							if (NotModelledRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setNotModelled, String.class, data, NotModelledRule.class);
							}
							if (OrganisationCountryRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setCountry, CountryEnum.class, data, OrganisationCountryRule.class);
							}
							if (OrganisationNameRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setName, String.class, data, OrganisationNameRule.class);
							}
							if (PowersRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setPowers, PowerEnum.class, data, PowersRule.class);
							}
							if (SpecialAbilitiesRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setHasSpecialAbilities, Boolean.class, data, SpecialAbilitiesRule.class);
							}
						}
					}
					
					return dataItemReportBuilder.build();
				}
				
				@Override
				public <T> List<SokoviaAccordsReport> buildReportList(Collection<GroupableData<?, T>> reportData) {
					List<SokoviaAccordsReport.SokoviaAccordsReportBuilder> listBuilder = new ArrayList();
					
					for (GroupableData<?, T> groupableData : reportData) {
						DataIdentifier dataIdentifier = groupableData.getIdentifier();
						if (dataIdentifier instanceof RuleIdentifier) {
							RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
							Class<?> ruleType = ruleIdentifier.getRuleType();
							Object data = groupableData.getData();
							if (data == null) {
								continue;
							}
							int index = ruleIdentifier.getRepeatableIndex().orElse(0);
							while (index >= listBuilder.size()) {
								listBuilder.add(null);
							}
							SokoviaAccordsReport.SokoviaAccordsReportBuilder dataItemReportBuilder = listBuilder.get(index);
							if (dataItemReportBuilder == null) {
								dataItemReportBuilder = SokoviaAccordsReport.builder();
								listBuilder.set(index, dataItemReportBuilder);
							}
							if (AttributeIntRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroInt, Integer.class, data, AttributeIntRule.class);
							}
							if (AttributeNumberRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroNumber, BigDecimal.class, data, AttributeNumberRule.class);
							}
							if (AttributeTimeRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroTime, LocalTime.class, data, AttributeTimeRule.class);
							}
							if (AttributeZonedDateTimeRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroZonedDateTime, ZonedDateTime.class, data, AttributeZonedDateTimeRule.class);
							}
							if (DateOfBirthRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setDateOfBirth, Date.class, data, DateOfBirthRule.class);
							}
							if (HeroNameRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setHeroName, String.class, data, HeroNameRule.class);
							}
							if (IsGovernmentAgencyRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setIsGovernmentAgency, Boolean.class, data, IsGovernmentAgencyRule.class);
							}
							if (NationalityRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setNationality, CountryEnum.class, data, NationalityRule.class);
							}
							if (NotModelledRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setNotModelled, String.class, data, NotModelledRule.class);
							}
							if (OrganisationCountryRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setCountry, CountryEnum.class, data, OrganisationCountryRule.class);
							}
							if (OrganisationNameRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setName, String.class, data, OrganisationNameRule.class);
							}
							if (PowersRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setPowers, PowerEnum.class, data, PowersRule.class);
							}
							if (SpecialAbilitiesRule.class.isAssignableFrom(ruleType)) {
								DataItemReportUtils.setField(dataItemReportBuilder::setHasSpecialAbilities, Boolean.class, data, SpecialAbilitiesRule.class);
							}
						}
					}
					
					return listBuilder.stream()
						.map((item) -> {
							if (item != null) {
								return item.build();
							}
							return null;
						})
						.collect(Collectors.toList());
				}
			}
			'''
			assertEquals(expected, reportBuilderJava)

		} finally {
		}
		code.compileToClasses
	}
	
	def loadBlueprint(Map<String, Class<?>> classes, String blueprintName) {
		val Class<?> bpClass  = classes.get(blueprintName)
		assertNotNull(bpClass)
		val RosettaActionFactory raf = mock(RosettaActionFactory)
		val Injector injector = Guice.createInjector([binder| {
			binder.bind(RosettaActionFactory).toInstance(raf);
		}]);
		return injector.getInstance(bpClass)
	}

	@Test
	def void validPath() {
		val blueprint = '''
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				baz string (1..1)
			
			reporting rule Blueprint1 from Foo:
				[legacy-syntax]
				extract Foo->bar then
				extract Bar->baz
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "validPath");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;

				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import javax.inject.Inject;

				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Foo, String, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Foo, String, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, actionFactory.<Foo, Bar, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.2/@nodes/@node", "Foo->bar", new RuleIdentifier("Foo->bar", getClass()), foo -> MapperS.of(foo).<Bar>map("getBar", _foo -> _foo.getBar())))
							.then(actionFactory.<Bar, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.2/@nodes/@next/@node", "Bar->baz", new RuleIdentifier("Bar->baz", getClass()), bar -> MapperS.of(bar).<String>map("getBaz", _bar -> _bar.getBaz())))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			assertEquals(expected, blueprintJava)
			val classes = blueprint.compileToClasses
			val bpImpl = classes.loadBlueprint("com.rosetta.test.model.blueprint.Blueprint1Rule")
			assertNotNull(bpImpl)
		} finally {
		}
	}

	@Test
	def void invalidPath() {
		'''
			type Input:
				input2 Input2 (1..1)
			
			type Input2:
				colour string (1..1)
			
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				extract Input->input2->name
		'''.parseRosetta.assertError(ROSETTA_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC,
			"Couldn't resolve reference to RosettaFeature 'name'.")
	}

	@Test
	def void brokenAndInputTypes() {
		'''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input->traderef,
					extract Input2->colour
				)
			
			type Input:
				traderef string (1..1)
			
			type Input2:
				colour string (1..1)
			
		'''.parseRosetta.assertError(BLUEPRINT_EXTRACT, RosettaIssueCodes.TYPE_ERROR,
			"Input type of Input2 is not assignable from type Input of previous node")
	}
	
	@Test
	def void multiplicationExpression() {
		val model = '''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				extract Input->traderef * Input2->colour
			
			type Input:
				traderef int (1..1)
			
			type Input2:
				colour int (1..1)
			
		'''.parseRosetta
		model.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR, 
			"Input types must be the same but were Input and Input2")
	}
	
	@Test
	def void brokenExpressionInputTypes() {
		val model = '''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				extract Input->traderef * Input->colour
			
			type Input:
				traderef int (1..1)
				colour int (1..1)
			
		'''.parseRosetta
		model.generateCode.compileToClasses
	}

	@Test
	def void orInputTypesExtends() {
		val code = '''
			reporting rule Blueprint1 from Input2:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input->traderef,
					extract Input2->colour
				)
			
			type Input:
				traderef string (1..1)
			
			type Input2 extends Input:
				colour string (1..1)
			
		'''.generateCode
		code.compileToClasses
	}

	@Test
	def void complexOr() {
		val blueprint = '''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					filter when Input->traderef="3" then extract Input->traderef, 
					extract Input->colour
				)
			
			type Input:
				traderef string (1..1)
				colour string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		 //writeOutClasses(blueprint, "complexOr");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;

				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.Filter;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input;
				import javax.inject.Inject;

				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;

				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, String, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, String, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input, String, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, new Filter<Input, INKEY>("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.0/@node", "Input->traderef = \"3\"", input -> areEqual(MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("3"), CardinalityOperator.All).get(), null))
								.then(actionFactory.<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.0/@next/@node", "Input->traderef", new RuleIdentifier("Input->traderef", getClass()), input -> MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()))),
								startsWith(actionFactory, actionFactory.<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.1/@node", "Input->colour", new RuleIdentifier("Input->colour", getClass()), input -> MapperS.of(input).<String>map("getColour", _input -> _input.getColour())))
								)
							)
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			assertEquals(expected, blueprintJava)
			blueprint.compileToClasses
		} finally {
		}
	}

	@Test
	def void numberOr() {
		val blueprint = '''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input->a , 
					extract Input->b
				)
			
			type Input:
				a int (1..1)
				b number (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "numberOr");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input;
				import java.math.BigDecimal;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, Number, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Number, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input, Number, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, actionFactory.<Input, Integer, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.0/@node", "Input->a", new RuleIdentifier("Input->a", getClass()), input -> MapperS.of(input).<Integer>map("getA", _input -> _input.getA()))),
								startsWith(actionFactory, actionFactory.<Input, Number, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.1/@node", "Input->b", new RuleIdentifier("Input->b", getClass()), input -> MapperS.of(input).<BigDecimal>map("getB", _input -> _input.getB())))
								)
							)
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			assertEquals(expected, blueprintJava)
			blueprint.compileToClasses
		} finally {
		}
	}

	@Test
	def void complexOr2() {
		val blueprint = '''
			reporting rule Blueprint1 from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input -> foo
					,
					extract Input -> bar
				)
			
			type Input:
				foo Foo (1..1)
				bar Bar (1..1)
			
			type Foo:
				traderef string (1..1)
				colour string (1..1)
			
			type Bar:
				traderef string (1..1)
				colour string (1..1)
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "complexOr2");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;

				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Input;
				import javax.inject.Inject;

				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, Object, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input, Object, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, actionFactory.<Input, Foo, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.0/@node", "Input->foo", new RuleIdentifier("Input->foo", getClass()), input -> MapperS.of(input).<Foo>map("getFoo", _input -> _input.getFoo()))),
								startsWith(actionFactory, actionFactory.<Input, Bar, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.1/@node", "Input->bar", new RuleIdentifier("Input->bar", getClass()), input -> MapperS.of(input).<Bar>map("getBar", _input -> _input.getBar())))
								)
							)
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void complexOr3() {
		val blueprint = '''
			reporting rule Blueprint1 from Input1:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input1->i1
					,
					extract Input1->i2
				) then
				extract Input2->traderef
						
			type Input1:
				i1 Input2 (1..1)
				i2 Input2 (1..1)
			
			type Input2:
				traderef string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "complexOr3");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input1;
				import com.rosetta.test.model.Input2;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input1, String, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input1, String, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input1, Input2, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, actionFactory.<Input1, Input2, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.0/@node", "Input1->i1", new RuleIdentifier("Input1->i1", getClass()), input1 -> MapperS.of(input1).<Input2>map("getI1", _input1 -> _input1.getI1()))),
								startsWith(actionFactory, actionFactory.<Input1, Input2, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.1/@node", "Input1->i2", new RuleIdentifier("Input1->i2", getClass()), input1 -> MapperS.of(input1).<Input2>map("getI2", _input1 -> _input1.getI2())))
								)
							)
							.then(actionFactory.<Input2, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@next/@node", "Input2->traderef", new RuleIdentifier("Input2->traderef", getClass()), input2 -> MapperS.of(input2).<String>map("getTraderef", _input2 -> _input2.getTraderef())))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void ruleRef() {
		val parsed = '''
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val string (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				Rule2 then
				extract Bar->val
			
			reporting rule Rule2 from Foo:
				[legacy-syntax]
				extract Foo->bar
			
		'''.parseRosettaWithNoErrors
		val classes= parsed.generateCode
		//.writeClasses("ruleRef")
		.compileToClasses
		val bpImpl = classes.loadBlueprint("com.rosetta.test.model.blueprint.Rule1Rule")
		assertNotNull(bpImpl)
	}

	@Test
	def void filter() {
		val blueprint = '''
			reporting rule SimpleBlueprint from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter when Input->traderef="Hello"
			
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		//blueprint.writeClasses("blueprint.filter")
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.blueprint;

		import com.regnosys.rosetta.blueprints.Blueprint;
		import com.regnosys.rosetta.blueprints.BlueprintInstance;
		import com.regnosys.rosetta.blueprints.runner.actions.Filter;
		import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Input;
		import javax.inject.Inject;

		import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;

		/**
		 * @version test
		 */
		public class SimpleBlueprintRule<INKEY> implements Blueprint<Input, Input, INKEY, INKEY> {
			
			private final RosettaActionFactory actionFactory;
			
			@Inject
			public SimpleBlueprintRule(RosettaActionFactory actionFactory) {
				this.actionFactory = actionFactory;
			}
			
			@Override
			public String getName() {
				return "SimpleBlueprint"; 
			}
			
			@Override
			public String getURI() {
				return "__synthetic1.rosetta#com.rosetta.test.model.SimpleBlueprint";
			}
			
			
			@Override
			public BlueprintInstance<Input, Input, INKEY, INKEY> blueprint() {
				return 
					startsWith(actionFactory, new Filter<Input, INKEY>("__synthetic1.rosetta#/0/@elements.0/@nodes/@node", "Input->traderef = \"Hello\"", input -> areEqual(MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("Hello"), CardinalityOperator.All).get(), null))
					.toBlueprint(getURI(), getName());
			}
		}
		'''
		assertEquals(expected, blueprintJava)
		blueprint.compileToClasses
	}

	@Test
	def void filter2() {
		val blueprint = '''
			reporting rule SimpleBlueprint from Input:
				[legacy-syntax]
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter when Input->traderef exists
									
			type Input:
				traderef string (0..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter2");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void filterWhenRule() {
		val blueprint = '''
			reporting rule TestRule from Input:
				[legacy-syntax]
				extract Input->flag
			
			reporting rule FilterRule from Input:
				[legacy-syntax]
				filter when rule TestRule then extract Input->traderef
			
			
			type Input:
				traderef string (1..1)
				flag boolean (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.FilterRuleRule")
		//writeClasses(blueprint, "filterWhenRule");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void lookupRule() {
		val blueprint = '''
			reporting rule WorthyAvenger from Avengers:
				[legacy-syntax]
				extract Avengers -> heros then 
				filter when rule CanWieldMjolnir 
					then filter when Hero -> name <> 'Thor'
					then extract Hero -> name
			
			eligibility rule CanWieldMjolnir from Hero:
				[legacy-syntax]
				lookup CanWieldMjolnir boolean
			
			type Avengers:
				heros Hero (0..*)
			
			type Hero:
				name string (1..1)
			
		'''.generateCode
		//blueprint.writeClasses("lookupRule")
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.WorthyAvengerRule")
		blueprint.compileToClasses

		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Filter;
			import com.regnosys.rosetta.blueprints.runner.actions.FilterByRule;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Avengers;
			import com.rosetta.test.model.Hero;
			import javax.inject.Inject;
			
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			/**
			 * @version test
			 */
			public class WorthyAvengerRule<INKEY> implements Blueprint<Avengers, String, INKEY, INKEY> {
				
				private final RosettaActionFactory actionFactory;
				
				@Inject
				public WorthyAvengerRule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "WorthyAvenger"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.WorthyAvenger";
				}
				
				@Inject protected CanWieldMjolnirRule canWieldMjolnirRef;
				
				@Override
				public BlueprintInstance<Avengers, String, INKEY, INKEY> blueprint() {
					return 
						startsWith(actionFactory, actionFactory.<Avengers, Hero, INKEY>newRosettaMultipleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node", "Avengers->heros", new RuleIdentifier("Avengers->heros", getClass()), avengers -> MapperS.of(avengers).<Hero>mapC("getHeros", _avengers -> _avengers.getHeros())))
						.then(new FilterByRule<Hero, INKEY>("__synthetic1.rosetta#/0/@elements.0/@nodes/@next/@node", "CanWieldMjolnir",
											getCanWieldMjolnir(), null))
						.then(new Filter<Hero, INKEY>("__synthetic1.rosetta#/0/@elements.0/@nodes/@next/@next/@node", "Hero->name <> \"Thor\"", hero -> notEqual(MapperS.of(hero).<String>map("getName", _hero -> _hero.getName()), MapperS.of("Thor"), CardinalityOperator.Any).get(), null))
						.then(actionFactory.<Hero, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@next/@next/@next/@node", "Hero->name", new RuleIdentifier("Hero->name", getClass()), hero -> MapperS.of(hero).<String>map("getName", _hero -> _hero.getName())))
						.toBlueprint(getURI(), getName());
				}
				
				protected BlueprintInstance<Hero, Boolean, INKEY, INKEY> getCanWieldMjolnir() {
					return canWieldMjolnirRef.blueprint();
				}
			}
		'''
		assertEquals(expected, blueprintJava)

	}
	
	@Test
	def void filterCardinalityAndType() {
		val model = '''
			reporting rule TestRule from Input:
				[legacy-syntax]
				extract Input->flag
						
			reporting rule FilterRule from Input:
				[legacy-syntax]
				filter when rule TestRule then extract Input->traderef
			
			
			type Input:
				traderef string (1..1)
				flag string (1..*)
			
		'''.parseRosetta
		
		model.assertError(BLUEPRINT_REF, RosettaIssueCodes.TYPE_ERROR,
			"output type of node string does not match required type of boolean")
		model.assertError(BLUEPRINT_FILTER, null,
			"The expression for Filter must return a single value but the rule TestRule can return multiple values")
	}

	@Test
	def void filterWhenCount() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				[legacy-syntax]
				extract Foo->fixed count = 12
			
			type Foo:
				fixed string (0..*)
				floating string (0..*)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.IsFixedFloatRule")
		// writeOutClasses(blueprint, "filterWhenCount");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;

			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Foo;
			import javax.inject.Inject;

			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;

			/**
			 * @version test
			 */
			public class IsFixedFloatRule<INKEY> implements Blueprint<Foo, Boolean, INKEY, INKEY> {
				
				private final RosettaActionFactory actionFactory;
				
				@Inject
				public IsFixedFloatRule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "IsFixedFloat"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.IsFixedFloat";
				}
				
				
				@Override
				public BlueprintInstance<Foo, Boolean, INKEY, INKEY> blueprint() {
					return 
						startsWith(actionFactory, actionFactory.<Foo, Boolean, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node", "Foo->fixed count = 12", new RuleIdentifier("Foo->fixed count = 12", getClass()), foo -> areEqual(MapperS.of(MapperS.of(foo).<String>mapC("getFixed", _foo -> _foo.getFixed()).resultCount()), MapperS.of(Integer.valueOf(12)), CardinalityOperator.All)))
						.toBlueprint(getURI(), getName());
				}
			}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	def void expressionIf() {
		val blueprint = '''
			reporting rule FixedFloat from Foo:
				[legacy-syntax]
				extract 
					if Foo -> fixed = "Wood" then
						Foo -> floating
					else if Foo -> fixed = "Steel" then
						Foo -> sinking
					else 
						Foo -> swimming
			
			type Foo:
				fixed string (1..1)
				floating string (1..1)
				sinking string (1..1)
				swimming string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.FixedFloatRule")
		// writeOutClasses(blueprint, "expressionIf");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		blueprint.compileToClasses
	}
	
	@Test
	def void maxBy() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				[legacy-syntax]
				extract Foo -> bars 
					max [ item -> order ]
			
			type Foo:
				bars Bar (0..*)
			
			type Bar:
				order int (0..1)
			
		'''.generateCode
		// writeOutClasses(blueprint, "maxBy");
		blueprint.compileToClasses
	}
	
	@Test
	@Disabled
	def void maxBy2() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				[legacy-syntax]
				extract Foo -> bars then
				extract Bar max [ item -> order ] // should this work?
			
			type Foo:
				bars Bar (0..*)
			
			type Bar:
				order int (0..1)
			
		'''.generateCode
		// writeOutClasses(blueprint, "maxBy");
		blueprint.compileToClasses
	}
	
	@Test
	def void maxBy3() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				[legacy-syntax]
				extract Foo -> bars -> order max
			
			type Foo:
				bars Bar (0..*)
			
			type Bar:
				order int (0..1)
			
		'''.generateCode
		// writeOutClasses(blueprint, "maxBy");
		blueprint.compileToClasses
	}
	
	@Test
	def void maxByBrokenType() {
		val model = '''
			reporting rule IsFixedFloat from Foo:
				[legacy-syntax]
				extract Foo -> bars 
					max [ item ]
			
			type Foo:
				bars Bar (0..*)
			
			type Bar:
				order int (0..1)
			
		'''.parseRosetta
		model.assertError(INLINE_FUNCTION, null,
			"Operation max only supports comparable types (string, int, string, date). Found type Bar.")
	}

	@Test
	def void expressionBadTypes() {
		 ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				extract Foo->bar->val + Bar->val
			'''.parseRosetta
			.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR,
			"Input types must be the same but were Foo and Bar")
	}

	@Test
	def void functionCallFromExtract() {
		val blueprint = ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				return MyFunc1() then
				extract MyFunc(Foo->bar->val)
			
			func MyFunc1: 
				output:
					foo Foo (1..1)
			
			func MyFunc:
				inputs: 
					foo number (0..1)
				output: 
					bar number (1..1)
				set bar:
					foo + 1
				
			'''.parseRosettaWithNoErrors
			.generateCode
			//blueprint.writeClasses("functionCall")
			blueprint.compileToClasses
	}
	
	@Test
	def void functionCallsFromExtract() {
		val blueprint = ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				return MyFunc1() then
				extract MyFunc(Foo->bar->val) > MyFunc(3.0)
			
			func MyFunc1: 
				output:
					foo Foo (1..1)
			
			func MyFunc:
				inputs: 
					foo number (0..1)
				output: 
					bar number (1..1)
				set bar:
					foo + 1
				
			'''.parseRosettaWithNoErrors
			.generateCode
			//blueprint.writeClasses("functionCallsFromExtract")
			blueprint.compileToClasses
	}

	@Test
	def void functionCallsWithLiteralInputFromExtract() {
		val blueprint = ''' 
			reporting rule FooRule from Foo:
				[legacy-syntax]
				extract 
					if FooFunc( Foo -> a, "x" ) then "Y"
					else "Z"
			
			type Foo:
				a string (1..1)
			
			func FooFunc:
				inputs:
					a string (1..1)
					b string (1..1)
				output:
					result boolean (1..1)
			'''.parseRosettaWithNoErrors
			.generateCode
			//blueprint.writeClasses("functionCallsFromExtract")
			blueprint.compileToClasses
	}

	
	@Test
	def void functionCallFromExtractBadTypes() {
		 ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				extract MyFunc(Foo->bar->val, Bar->val)
			
			func MyFunc:
				inputs: 
					a number (0..1)
					b number (0..1)
				output: 
					r number (1..1)
				set r:
					a + b
				
			'''.parseRosetta
			.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR,
			"Input types must be the same but were [Foo, Bar]")
	}
	
	@Test
	def void functionCallFromFilterWhen() {
		val blueprint = ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				return MyFunc1() then
				filter when MyFunc(Foo->bar->val)
			
			func MyFunc1: 
				output:
					foo Foo (1..1)
			
			func MyFunc:
				inputs: 
					foo number (0..1)
				output: 
					result boolean (1..1)
				set result:
					foo > 1
			
			'''.parseRosettaWithNoErrors
			.generateCode
			//blueprint.writeClasses("functionCallFromFilterWhen")
			blueprint.compileToClasses
	}
	
	@Test
	def void functionCallFromFilterWhenBadTypes() {
		 ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				return MyFunc1() then
				filter when MyFunc(Foo->bar->val)
			
			func MyFunc1:
				output:
					foo Foo (1..*)
			
			func MyFunc:
				inputs: 
					foo number (0..1)
				output: 
					result number (1..1)
				set result:
					foo + 1
			
			'''.parseRosetta
			.assertError(BLUEPRINT_FILTER, RosettaIssueCodes.TYPE_ERROR,
			"The expression for Filter must return a boolean the current expression returns number")
	}
	
	@Test
	def void shouldUseBlueprintFromDifferentNS() {
		val code = #['''
			namespace ns1
			
			
			type TestObject: 
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				[legacy-syntax]
				extract TestObject -> fieldOne	
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2 from TestObject:
				[legacy-syntax]
				Rule1
		'''
		].generateCode
		//code.writeClasses("shouldUseBlueprintFromDifferentNS")
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("ns2.blueprint.Rule2Rule")
		assertNotNull(bpImpl)
	}
	 
	@Test
	def void shouldUseBlueprintRuleFromDifferentNS() {
		val code = #['''
			namespace ns1
			
			
			type TestObject: 
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				[legacy-syntax]
				extract TestObject -> fieldOne exists
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2 from TestObject:
				[legacy-syntax]
				filter when rule Rule1
		'''
		].generateCode
		//code.writeClasses("shouldUseBlueprintRuleFromDifferentNS")
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("ns2.blueprint.Rule2Rule")
		assertNotNull(bpImpl)
	}
	
	@Test
	def void callRuleWithAs() {
		val code = '''
			
			
			type TestObject: <"">
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				[legacy-syntax]
				extract TestObject -> fieldOne
							
			reporting rule Rule2 from TestObject:
				[legacy-syntax]
				Rule1 as "BLAH"
			
		'''
		.generateCode
		//code.writeClasses("callRuleWithAs")
		code.compileToClasses
		
	}
	
	@Test
	def void brokenRuleTypes() {
		'''
			type Foo: <"">
				bar Bar (0..1)
			
			type Bar:
				str string (1..1)
			
			reporting rule Rule1 from Foo:
				[legacy-syntax]
				extract Foo->bar then
				Rule2
							
			reporting rule Rule2 from Foo:
				[legacy-syntax]
				extract Foo->bar->str
			
		'''.toString
		.replace('\r', "")
		.parseRosetta
			.assertError(BLUEPRINT_REF, RosettaIssueCodes.TYPE_ERROR,
			"Input type of Foo is not assignable from type Bar of previous node")
		
	}


	@Test
	def void longNestedIfElseWithReturn0() {
		var blueprint = '''
		type Foo:
			bar Bar (1..1)
		
		enum Bar:
			A B C D F G H I J K L M N O P Q R S T U V W X Y Z
		
		reporting rule BarField from Foo:
				[legacy-syntax]
				extract if Foo -> bar = Bar -> A then "A"
					else if Foo -> bar = Bar -> B then "B"
					else if Foo -> bar = Bar -> C then "C"
					else if Foo -> bar = Bar -> D then "D"
					else if Foo -> bar = Bar -> F then "F"
					else if Foo -> bar = Bar -> G then "G"
					else if Foo -> bar = Bar -> H then "H"
					else if Foo -> bar = Bar -> I then "I"
					else if Foo -> bar = Bar -> B then "B"
					else if Foo -> bar = Bar -> C then "C"
					else if Foo -> bar = Bar -> D then "D"
					else if Foo -> bar = Bar -> F then "F"
					else if Foo -> bar = Bar -> G then "G"
					else if Foo -> bar = Bar -> H then "H"
					else if Foo -> bar = Bar -> I then "I"
					else if Foo -> bar = Bar -> J then "J"
					else if Foo -> bar = Bar -> K then "K"
					else if Foo -> bar = Bar -> L then "L"
					else if Foo -> bar = Bar -> M then "M"
					else if Foo -> bar = Bar -> N then "N"
					else if Foo -> bar = Bar -> O then "O"
					else if Foo -> bar = Bar -> P then "P"
					else if Foo -> bar = Bar -> Q then "Q"
					else if Foo -> bar = Bar -> R then "R"
					else if Foo -> bar = Bar -> S then "S"
					else if Foo -> bar = Bar -> T then "T"
					else if Foo -> bar = Bar -> U then "U"
					else if Foo -> bar = Bar -> V then "V"
					else if Foo -> bar = Bar -> W then "W"
					else if Foo -> bar = Bar -> X then "X"
					else if Foo -> bar = Bar -> Y then "Y"
					else if Foo -> bar = Bar -> Z then "Z"
					else "0"
			
		'''.toString
		.replace('\r', "")
		.generateCode
		//blueprint.writeClasses("longNestedIfElseWithReturn0");
		blueprint.compileToClasses
	}

	@Test
	def void longNestedIfElseWithNoReturn() {
		var blueprint = '''
		type Foo:
			bar Bar (1..1)
		
		enum Bar:
			A B C D F G H I J K L M N O P Q R S T U V W X Y Z
		
		reporting rule BarField from Foo:
				[legacy-syntax]
				extract if Foo -> bar = Bar -> A then "A"
					else if Foo -> bar = Bar -> B then "B"
					else if Foo -> bar = Bar -> C then "C"
					else if Foo -> bar = Bar -> D then "D"
					else if Foo -> bar = Bar -> F then "F"
					else if Foo -> bar = Bar -> G then "G"
					else if Foo -> bar = Bar -> H then "H"
					else if Foo -> bar = Bar -> I then "I"
					else if Foo -> bar = Bar -> B then "B"
					else if Foo -> bar = Bar -> C then "C"
					else if Foo -> bar = Bar -> D then "D"
					else if Foo -> bar = Bar -> F then "F"
					else if Foo -> bar = Bar -> G then "G"
					else if Foo -> bar = Bar -> H then "H"
					else if Foo -> bar = Bar -> I then "I"
					else if Foo -> bar = Bar -> J then "J"
					else if Foo -> bar = Bar -> K then "K"
					else if Foo -> bar = Bar -> L then "L"
					else if Foo -> bar = Bar -> M then "M"
					else if Foo -> bar = Bar -> N then "N"
					else if Foo -> bar = Bar -> O then "O"
					else if Foo -> bar = Bar -> P then "P"
					else if Foo -> bar = Bar -> Q then "Q"
					else if Foo -> bar = Bar -> R then "R"
					else if Foo -> bar = Bar -> S then "S"
					else if Foo -> bar = Bar -> T then "T"
					else if Foo -> bar = Bar -> U then "U"
					else if Foo -> bar = Bar -> V then "V"
					else if Foo -> bar = Bar -> W then "W"
					else if Foo -> bar = Bar -> X then "X"
					else if Foo -> bar = Bar -> Y then "Y"
					else if Foo -> bar = Bar -> Z then "Z"
			
		'''.toString
			.replace('\r', "")
			.generateCode
			//blueprint.writeClasses("longNestedIfElseWithNoReturn");
		blueprint.compileToClasses
	}

	@Test
	def void ifWithSingleCardinality() {
		var blueprint = '''
		type Foo:
			test boolean (1..1)
			bar Bar (1..1)
			bar2 Bar (1..1)
		
		type Bar:
			attr string (1..1)
		
		reporting rule BarField from Foo:
			[legacy-syntax]
			extract 
				if Foo->test = True 
				then Foo->bar
				else Foo->bar2
		
		'''.generateCode
		//blueprint.writeClasses("ifWithSingleCardinality")
		blueprint.compileToClasses
	}
	
	@Test
	def void ifWithMultipleCardinality() {
		var blueprint = '''
		type Foo:
			test boolean (1..1)
			bar Bar (1..*)
			bar2 Bar (1..*)
		
		type Bar:
			attr string (1..1)
		
		reporting rule BarField from Foo:
			[legacy-syntax]
			extract 
				if Foo->test = True 
				then Foo->bar
				else Foo->bar2
		
		'''.generateCode
		//blueprint.writeClasses("ifWithSingleCardinality")
		blueprint.compileToClasses
	}


	@Test
	def void reportingRuleWithoutBodyDoesNotGenerateCode() {
		var blueprint = '''
		
		reporting rule BarField from ReportableEvent:
			[legacy-syntax]

		'''.toString
		.replace('\r', "")
		.generateCode
		// blueprint.writeClasses("reportingRuleWithoutBodyDoesNotGenerateCode");
			
		blueprint.compileToClasses
		
	}

	@Test
	def void shouldGenerateDataType() {
		val blueprint = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			from Bar
			when FooRule
			with type BarReport
			
			eligibility rule FooRule from Bar:
				[legacy-syntax]
				filter when Bar->barA exists
			
			reporting rule Aa from Bar:
				[legacy-syntax]
				extract Bar->barA as "A"

			reporting rule Bb from Bar:
				[legacy-syntax]
				extract Bar->barB as "B"
				
			reporting rule Cc from Bar:
				[legacy-syntax]
				extract Bar->barC as "C"

			reporting rule Dd from Bar:
				[legacy-syntax]
				extract Bar->barD as "D"

			reporting rule Ee from Bar:
				[legacy-syntax]
				extract Bar->barE as "E"
				
			reporting rule Ff from Bar:
				[legacy-syntax]
				extract Bar->barF as "F"
			
			type Bar:
				barA date (0..1)
				barB time (0..1)
				barC zonedDateTime (0..1)
				barD int (0..1)
				barE number (0..1)
				barF BazEnum (0..1)

			enum BazEnum:
				X
				Y
				Z
			
			type BarReport:
				aa date (1..1)
					[ruleReference Aa]
				bb time (1..1)
					[ruleReference Bb]
				cc zonedDateTime (1..1)
					[ruleReference Cc]
				dd int (1..1)
					[ruleReference Dd]
				ee number (1..1)
					[ruleReference Ee]
				ff BazEnum (1..1)
					[ruleReference Ff]
			
		'''.generateCode
		blueprint.compileToClasses
	}
	
	@Test
	def void shouldTypeResolutionForListOperation() {
		var blueprint = '''
		type Foo:
			bar Bar (0..*)
		
		type Bar:
			attr string (1..1)
		
		reporting rule FooRule from Foo:
			[legacy-syntax]
			extract Foo -> bar 
				max [ item -> attr ] then
			extract Bar -> attr
		
		'''.generateCode
		blueprint.compileToClasses
	}
	
	@Test
	def void shouldTypeResolutionForListOperation2() {
		var blueprint = '''
		type Foo:
			bar Bar (0..*)
		
		type Bar:
			baz Baz (1..1)
		
		type Baz:
			attr string (1..1)
		
		reporting rule FooRule from Foo:
			[legacy-syntax]
			extract Foo -> bar 
				map [ item -> baz ] then
			extract Baz -> attr
		
		'''.generateCode
		blueprint.compileToClasses
	}

	@Test
	def void shouldReturnEnumValue() {
		val code = '''
			enum FooEnum:
				Bar
				Baz
			
			reporting rule ReturnEnumValue from number:
				[legacy-syntax]
				return FooEnum -> Bar
			
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldNameNonLegacyReportsCorrectly() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR1
					corpus TEST_REG MiFIR2

					report TEST_REG MiFIR1 MiFIR2 in T+1
					from Bar
					when FooRule
					with type BarReport

					type BarReport:
						barBarOne string (1..1)


				''']
		val code = model.generateCode
		
		val reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIR1MiFIR2ReportFunction")
		assertThat(reportJava, CoreMatchers.notNullValue())
	
	}
	
	@Test
	def void shouldNameLegacyReportsCorrectly() {
		val model = #[
				REPORT_TYPES,
				REPORT_RULES,
				'''
					namespace com.rosetta.test.model

					body Authority TEST_REG
					corpus TEST_REG MiFIR1
					corpus TEST_REG MiFIR2

					report TEST_REG MiFIR1 MiFIR2 in T+1
					from Bar
					when FooRule
					with type BarReport

					type BarReport:
						barBarOne string (1..1)


				''']
		val code = model.generateCode
		
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIR1_MiFIR2BlueprintReport")
		assertThat(reportJava, CoreMatchers.notNullValue())
	
	}
}
