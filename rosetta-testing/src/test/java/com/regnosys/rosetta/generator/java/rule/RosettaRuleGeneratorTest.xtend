package com.regnosys.rosetta.generator.java.rule

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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock
import javax.inject.Inject

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class RosettaRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

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
						filter bar1 exists

					reporting rule BarBarOne from Bar:
						extract bar1 as "1 BarOne"

					reporting rule BarBarTwo from Bar:
						extract bar2 as "2 BarTwo"

					reporting rule BarBaz from Bar:
						extract baz->baz1 as "3 BarBaz"

					reporting rule BarQuxList from Bar:
						extract quxList
						then extract BarQuxReport {
							bazQux1: QuxQux1,
							bazQux2: QuxQux2
						} as "4 BarQuxList"

					reporting rule QuxQux1 from Qux:
						extract qux1 as "5 QuxQux1"

					reporting rule QuxQux2 from Qux:
						extract qux2 as "6 QuxQux2"

					reporting rule BarQuux from Bar:
						extract Create_Quux( baz ) as "7 BarQuux"

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
						barBarTwo string (0..*)
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
								.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).getMulti());
							
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setListField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
								if (BarBazRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
								}
								if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Quux>setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
								}
								if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
								}
								if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setListField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
								if (BarBazRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
								}
								if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Quux>setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
								}
								if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
								}
								if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
								if (BarBazRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
								}
								if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Quux>setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
								}
								if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
								}
								if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
								if (BarBazRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
								}
								if (BarQuuxRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Quux>setField(dataItemReportBuilder::setBarQuux, Quux.class, data, BarQuuxRule.class);
								}
								if (QuxQux1Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux1, String.class, data, QuxQux1Rule.class);
								}
								if (QuxQux2Rule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateBarQuxList(ruleIdentifier.getRepeatableIndex().orElse(0))::setBazQux2, String.class, data, QuxQux2Rule.class);
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
						barBarTwo string (0..*)
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
						extract bar1 + "NEW" as "1 New BarOne"

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
								.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).getMulti());
							
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
						barBarTwo string (0..*)
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
								.setBarBarTwo(MapperC.<String>of(barBarTwo.evaluate(MapperS.of(input).get())).getMulti());
							
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
				extract bar2 as "2 BarTwo"
			
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
				filter bar1 exists
			
			reporting rule BarBarOne from Bar:
				extract bar1 as "1 BarOne"
			
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
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
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
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
				filter bar1 exists
			
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
			    powers PowerEnum (0..*) <"Enum type - multiple cardinality">
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
			     filter hasSpecialAbilities
			
			reporting rule HeroName from Person: <"Name">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "1" provision "Hero Name."]
			    extract name as "Hero Name"
			
			reporting rule DateOfBirth from Person: <"Date of birth">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Date of birth."]
			    extract dateOfBirth as "Date of Birth"
			
			reporting rule Nationality from Person: <"Nationality">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Nationality."]
			    extract nationality as "Nationality"
			
			reporting rule SpecialAbilities from Person: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "3" provision "Has Special Abilities"]
			    extract hasSpecialAbilities as "Has Special Abilities"
			
			reporting rule Powers from Person: <"Super Power Name">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Powers."]
			    extract powers as "Powers"
			
			reporting rule AttributeInt from Person: <"Attribute - Int">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Int."]
			    extract attribute -> heroInt as "Attribute - Int"
			
			reporting rule AttributeNumber from Person: <"Attribute - Number">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Number."]
			    extract attribute -> heroNumber as "Attribute - Number"
			
			reporting rule AttributeZonedDateTime from Person: <"Attribute - ZonedDateTime">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - ZonedDateTime."]
			    extract attribute -> heroZonedDateTime as "Attribute - ZonedDateTime"
			
			reporting rule AttributeTime from Person: <"Attribute - Time">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Time."]
			    extract attribute -> heroTime as "Attribute - Time"
			
			reporting rule HeroOrganisations from Person: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract organisations 
			    then extract OrganisationReport {
			    	name: OrganisationName,
			    	country: OrganisationCountry,
			    	isGovernmentAgency: IsGovernmentAgency
			    } as "Hero Organisations"
			
			reporting rule OrganisationName from Organisation: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract name as "Organisation Name"
			
			reporting rule OrganisationCountry from Organisation: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract country as "Organisation Country"
			
			reporting rule IsGovernmentAgency from Organisation: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract isGovernmentAgency as "Is Government Agency"
			
			reporting rule NotModelled from Person: <"Not Modelled">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "6"  provision "Not Modelled."]
			    "Not modelled" as "Not Modelled"
			
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
									DataItemReportUtils.<Integer>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroInt, Integer.class, data, AttributeIntRule.class);
								}
								if (AttributeNumberRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<BigDecimal>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroNumber, BigDecimal.class, data, AttributeNumberRule.class);
								}
								if (AttributeTimeRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<LocalTime>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroTime, LocalTime.class, data, AttributeTimeRule.class);
								}
								if (AttributeZonedDateTimeRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<ZonedDateTime>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroZonedDateTime, ZonedDateTime.class, data, AttributeZonedDateTimeRule.class);
								}
								if (DateOfBirthRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Date>setField(dataItemReportBuilder::setDateOfBirth, Date.class, data, DateOfBirthRule.class);
								}
								if (HeroNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setHeroName, String.class, data, HeroNameRule.class);
								}
								if (IsGovernmentAgencyRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Boolean>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setIsGovernmentAgency, Boolean.class, data, IsGovernmentAgencyRule.class);
								}
								if (NationalityRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<CountryEnum>setField(dataItemReportBuilder::setNationality, CountryEnum.class, data, NationalityRule.class);
								}
								if (NotModelledRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setNotModelled, String.class, data, NotModelledRule.class);
								}
								if (OrganisationCountryRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<CountryEnum>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setCountry, CountryEnum.class, data, OrganisationCountryRule.class);
								}
								if (OrganisationNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setName, String.class, data, OrganisationNameRule.class);
								}
								if (PowersRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<PowerEnum>setListField(dataItemReportBuilder::setPowers, PowerEnum.class, data, PowersRule.class);
								}
								if (SpecialAbilitiesRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Boolean>setField(dataItemReportBuilder::setHasSpecialAbilities, Boolean.class, data, SpecialAbilitiesRule.class);
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
									DataItemReportUtils.<Integer>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroInt, Integer.class, data, AttributeIntRule.class);
								}
								if (AttributeNumberRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<BigDecimal>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroNumber, BigDecimal.class, data, AttributeNumberRule.class);
								}
								if (AttributeTimeRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<LocalTime>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroTime, LocalTime.class, data, AttributeTimeRule.class);
								}
								if (AttributeZonedDateTimeRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<ZonedDateTime>setField(dataItemReportBuilder.getOrCreateAttribute()::setHeroZonedDateTime, ZonedDateTime.class, data, AttributeZonedDateTimeRule.class);
								}
								if (DateOfBirthRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Date>setField(dataItemReportBuilder::setDateOfBirth, Date.class, data, DateOfBirthRule.class);
								}
								if (HeroNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setHeroName, String.class, data, HeroNameRule.class);
								}
								if (IsGovernmentAgencyRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Boolean>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setIsGovernmentAgency, Boolean.class, data, IsGovernmentAgencyRule.class);
								}
								if (NationalityRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<CountryEnum>setField(dataItemReportBuilder::setNationality, CountryEnum.class, data, NationalityRule.class);
								}
								if (NotModelledRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder::setNotModelled, String.class, data, NotModelledRule.class);
								}
								if (OrganisationCountryRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<CountryEnum>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setCountry, CountryEnum.class, data, OrganisationCountryRule.class);
								}
								if (OrganisationNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<String>setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setName, String.class, data, OrganisationNameRule.class);
								}
								if (PowersRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<PowerEnum>setListField(dataItemReportBuilder::setPowers, PowerEnum.class, data, PowersRule.class);
								}
								if (SpecialAbilitiesRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.<Boolean>setField(dataItemReportBuilder::setHasSpecialAbilities, Boolean.class, data, SpecialAbilitiesRule.class);
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
				extract item->bar
				then extract item->baz
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.reports.Blueprint1Rule")
		// writeOutClasses(blueprint, "validPath");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.Foo;
			
			
			@ImplementedBy(Blueprint1Rule.Blueprint1RuleDefault.class)
			public abstract class Blueprint1Rule implements ReportFunction<Foo, String> {
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public String evaluate(Foo input) {
					String output = doEvaluate(input);
					
					return output;
				}
			
				protected abstract String doEvaluate(Foo input);
			
				public static class Blueprint1RuleDefault extends Blueprint1Rule {
					@Override
					protected String doEvaluate(Foo input) {
						String output = null;
						return assignOutput(output, input);
					}
					
					protected String assignOutput(String output, Foo input) {
						output = MapperS.of(input)
							.mapSingleToItem(item -> (MapperS<Bar>)item.<Bar>map("getBar", foo -> foo.getBar()))
							.apply(item -> item
								.mapSingleToItem(_item -> (MapperS<String>)_item.<String>map("getBaz", bar -> bar.getBaz()))).get();
						
						return output;
					}
				}
			}
			'''
			assertEquals(expected, blueprintJava)
			val classes = blueprint.compileToClasses
			val bpImpl = classes.loadBlueprint("com.rosetta.test.model.reports.Blueprint1Rule")
			assertNotNull(bpImpl)
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
				Rule2 then
				extract val
			
			reporting rule Rule2 from Foo:
				extract bar
			
		'''.parseRosettaWithNoErrors
		val code = parsed.generateCode
		val bp = code.get("com.rosetta.test.model.reports.Rule1Rule")
		assertThat(bp, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.reports;
		
		import com.google.inject.ImplementedBy;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.reports.ReportFunction;
		import com.rosetta.test.model.Foo;
		import javax.inject.Inject;
		
		
		@ImplementedBy(Rule1Rule.Rule1RuleDefault.class)
		public abstract class Rule1Rule implements ReportFunction<Foo, String> {
			
			// RosettaFunction dependencies
			//
			@Inject protected Rule2Rule rule2;
		
			/**
			* @param input 
			* @return output 
			*/
			@Override
			public String evaluate(Foo input) {
				String output = doEvaluate(input);
				
				return output;
			}
		
			protected abstract String doEvaluate(Foo input);
		
			public static class Rule1RuleDefault extends Rule1Rule {
				@Override
				protected String doEvaluate(Foo input) {
					String output = null;
					return assignOutput(output, input);
				}
				
				protected String assignOutput(String output, Foo input) {
					output = MapperS.of(rule2.evaluate(MapperS.of(input).get()))
						.apply(item -> item
							.mapSingleToItem(_item -> (MapperS<String>)_item.<String>map("getVal", bar -> bar.getVal()))).get();
					
					return output;
				}
			}
		}
		'''
		assertEquals(expected, bp)
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("com.rosetta.test.model.reports.Rule1Rule")
		assertNotNull(bpImpl)
	}

	@Test
	def void filter() {
		val blueprint = '''
			reporting rule SimpleBlueprint from Input:
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter traderef="Hello"
			
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		//blueprint.writeClasses("blueprint.filter")
		val blueprintJava = blueprint.get("com.rosetta.test.model.reports.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.reports;
		
		import com.google.inject.ImplementedBy;
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.functions.ModelObjectValidator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.reports.ReportFunction;
		import com.rosetta.test.model.Input;
		import com.rosetta.test.model.Input.InputBuilder;
		import java.util.Optional;
		import javax.inject.Inject;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		@ImplementedBy(SimpleBlueprintRule.SimpleBlueprintRuleDefault.class)
		public abstract class SimpleBlueprintRule implements ReportFunction<Input, Input> {
			
			@Inject protected ModelObjectValidator objectValidator;
		
			/**
			* @param input 
			* @return output 
			*/
			@Override
			public Input evaluate(Input input) {
				Input.InputBuilder outputBuilder = doEvaluate(input);
				
				final Input output;
				if (outputBuilder == null) {
					output = null;
				} else {
					output = outputBuilder.build();
					objectValidator.validate(Input.class, output);
				}
				
				return output;
			}
		
			protected abstract Input.InputBuilder doEvaluate(Input input);
		
			public static class SimpleBlueprintRuleDefault extends SimpleBlueprintRule {
				@Override
				protected Input.InputBuilder doEvaluate(Input input) {
					Input.InputBuilder output = Input.builder();
					return assignOutput(output, input);
				}
				
				protected Input.InputBuilder assignOutput(Input.InputBuilder output, Input input) {
					output = toBuilder(MapperS.of(input)
						.filterSingleNullSafe(item -> (Boolean)areEqual(item.<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("Hello"), CardinalityOperator.All).get()).get());
					
					return Optional.ofNullable(output)
						.map(o -> o.prune())
						.orElse(null);
				}
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
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter traderef exists
									
			type Input:
				traderef string (0..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.reports.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter2");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void filterWhenRule() {
		val blueprint = '''
			reporting rule TestRule from Input:
				extract item->flag
						
			reporting rule FilterRule from Input:
				filter TestRule then extract traderef
			
			type Input:
				traderef string (1..1)
				flag boolean (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.reports.FilterRuleRule")
		//writeClasses(blueprint, "filterWhenRule");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void filterWhenCount() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				extract fixed count = 12
			
			type Foo:
				fixed string (0..*)
				floating string (0..*)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.reports.IsFixedFloatRule")
		// writeOutClasses(blueprint, "filterWhenCount");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.reports;
		
		import com.google.inject.ImplementedBy;
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.reports.ReportFunction;
		import com.rosetta.test.model.Foo;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		@ImplementedBy(IsFixedFloatRule.IsFixedFloatRuleDefault.class)
		public abstract class IsFixedFloatRule implements ReportFunction<Foo, Boolean> {
		
			/**
			* @param input 
			* @return output 
			*/
			@Override
			public Boolean evaluate(Foo input) {
				Boolean output = doEvaluate(input);
				
				return output;
			}
		
			protected abstract Boolean doEvaluate(Foo input);
		
			public static class IsFixedFloatRuleDefault extends IsFixedFloatRule {
				@Override
				protected Boolean doEvaluate(Foo input) {
					Boolean output = null;
					return assignOutput(output, input);
				}
				
				protected Boolean assignOutput(Boolean output, Foo input) {
					output = MapperS.of(input)
						.mapSingleToItem(item -> (MapperS<Boolean>)areEqual(MapperS.of(item.<String>mapC("getFixed", foo -> foo.getFixed()).resultCount()), MapperS.of(Integer.valueOf(12)), CardinalityOperator.All).asMapper()).get();
					
					return output;
				}
			}
		}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	def void functionCallsWithLiteralInputFromExtract() {
		val blueprint = ''' 
			reporting rule FooRule from Foo:
				extract 
					if FooFunc( a, "x" ) then "Y"
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
	def void shouldUseBlueprintFromDifferentNS() {
		val code = #['''
			namespace ns1
			
			
			type TestObject: 
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				extract fieldOne
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2 from TestObject:
				Rule1
		'''
		].generateCode
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("ns2.reports.Rule2Rule")
		assertNotNull(bpImpl)
	}
	 
	@Test
	def void shouldUseBlueprintRuleFromDifferentNS() {
		val code = #['''
			namespace ns1
			
			
			type TestObject: 
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				extract fieldOne exists
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2 from TestObject:
				filter Rule1
		'''
		].generateCode
		//code.writeClasses("shouldUseBlueprintRuleFromDifferentNS")
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("ns2.reports.Rule2Rule")
		assertNotNull(bpImpl)
	}
	
	@Test
	def void callRuleWithAs() {
		val code = '''
			
			
			type TestObject: <"">
				fieldOne string (0..1)
			
			reporting rule Rule1 from TestObject:
				extract fieldOne
							
			reporting rule Rule2 from TestObject:
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
				extract bar then
				Rule2
							
			reporting rule Rule2 from Foo:
				extract bar->str
			
		'''.toString
		.replace('\r', "")
		.parseRosetta
			.assertError(ROSETTA_SYMBOL_REFERENCE, RosettaIssueCodes.TYPE_ERROR,
			"Expected type 'Foo' but was 'Bar'")
		
	}


	@Test
	def void longNestedIfElseWithReturn0() {
		var blueprint = '''
		type Foo:
			bar Bar (1..1)
		
		enum Bar:
			A B C D F G H I J K L M N O P Q R S T U V W X Y Z
		
		reporting rule BarField from Foo:
				extract if bar = Bar -> A then "A"
					else if bar = Bar -> B then "B"
					else if bar = Bar -> C then "C"
					else if bar = Bar -> D then "D"
					else if bar = Bar -> F then "F"
					else if bar = Bar -> G then "G"
					else if bar = Bar -> H then "H"
					else if bar = Bar -> I then "I"
					else if bar = Bar -> B then "B"
					else if bar = Bar -> C then "C"
					else if bar = Bar -> D then "D"
					else if bar = Bar -> F then "F"
					else if bar = Bar -> G then "G"
					else if bar = Bar -> H then "H"
					else if bar = Bar -> I then "I"
					else if bar = Bar -> J then "J"
					else if bar = Bar -> K then "K"
					else if bar = Bar -> L then "L"
					else if bar = Bar -> M then "M"
					else if bar = Bar -> N then "N"
					else if bar = Bar -> O then "O"
					else if bar = Bar -> P then "P"
					else if bar = Bar -> Q then "Q"
					else if bar = Bar -> R then "R"
					else if bar = Bar -> S then "S"
					else if bar = Bar -> T then "T"
					else if bar = Bar -> U then "U"
					else if bar = Bar -> V then "V"
					else if bar = Bar -> W then "W"
					else if bar = Bar -> X then "X"
					else if bar = Bar -> Y then "Y"
					else if bar = Bar -> Z then "Z"
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
				extract if bar = Bar -> A then "A"
					else if bar = Bar -> B then "B"
					else if bar = Bar -> C then "C"
					else if bar = Bar -> D then "D"
					else if bar = Bar -> F then "F"
					else if bar = Bar -> G then "G"
					else if bar = Bar -> H then "H"
					else if bar = Bar -> I then "I"
					else if bar = Bar -> B then "B"
					else if bar = Bar -> C then "C"
					else if bar = Bar -> D then "D"
					else if bar = Bar -> F then "F"
					else if bar = Bar -> G then "G"
					else if bar = Bar -> H then "H"
					else if bar = Bar -> I then "I"
					else if bar = Bar -> J then "J"
					else if bar = Bar -> K then "K"
					else if bar = Bar -> L then "L"
					else if bar = Bar -> M then "M"
					else if bar = Bar -> N then "N"
					else if bar = Bar -> O then "O"
					else if bar = Bar -> P then "P"
					else if bar = Bar -> Q then "Q"
					else if bar = Bar -> R then "R"
					else if bar = Bar -> S then "S"
					else if bar = Bar -> T then "T"
					else if bar = Bar -> U then "U"
					else if bar = Bar -> V then "V"
					else if bar = Bar -> W then "W"
					else if bar = Bar -> X then "X"
					else if bar = Bar -> Y then "Y"
					else if bar = Bar -> Z then "Z"
			
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
			extract 
				if test
				then bar
				else bar2
		
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
			extract 
				if test
				then bar
				else bar2
		
		'''.generateCode
		//blueprint.writeClasses("ifWithSingleCardinality")
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
				filter barA exists
			
			reporting rule Aa from Bar:
				extract barA as "A"

			reporting rule Bb from Bar:
				extract barB as "B"
				
			reporting rule Cc from Bar:
				extract barC as "C"

			reporting rule Dd from Bar:
				extract barD as "D"

			reporting rule Ee from Bar:
				extract barE as "E"
				
			reporting rule Ff from Bar:
				extract barF as "F"
			
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
			extract [
				item -> bar 
					max [ item -> attr ]
			] then extract item -> attr
		
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
			extract [ 
				item -> bar 
					extract item -> baz
			] 
			then extract item -> attr
		
		'''.generateCode
		blueprint.compileToClasses
	}

	@Test
	def void shouldReturnEnumValue() {
		val code = '''
			enum FooEnum:
				Bar
				Baz
			
			reporting rule ReturnEnumValue from string:
				FooEnum -> Bar
			
		'''.generateCode
		
		val rule = code.get("com.rosetta.test.model.reports.ReturnEnumValueRule")
		
		val expectedRule = '''
		package com.rosetta.test.model.reports;
		
		import com.google.inject.ImplementedBy;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.reports.ReportFunction;
		import com.rosetta.test.model.FooEnum;
		
		
		@ImplementedBy(ReturnEnumValueRule.ReturnEnumValueRuleDefault.class)
		public abstract class ReturnEnumValueRule implements ReportFunction<String, FooEnum> {
		
			/**
			* @param input 
			* @return output 
			*/
			@Override
			public FooEnum evaluate(String input) {
				FooEnum output = doEvaluate(input);
				
				return output;
			}
		
			protected abstract FooEnum doEvaluate(String input);
		
			public static class ReturnEnumValueRuleDefault extends ReturnEnumValueRule {
				@Override
				protected FooEnum doEvaluate(String input) {
					FooEnum output = null;
					return assignOutput(output, input);
				}
				
				protected FooEnum assignOutput(FooEnum output, String input) {
					output = MapperS.of(FooEnum.BAR).get();
					
					return output;
				}
			}
		}
		'''
		
		assertEquals(expectedRule, rule)
		
		code.compileToClasses
	}
}
