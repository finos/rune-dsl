package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Guice
import com.google.inject.Injector
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
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.reports.ReportFunction;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.BarReport.BarReportBuilder;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
				@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
				public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected BarBarOneRule barBarOneRule;
					@Inject protected BarBarTwoRule barBarTwoRule;
					@Inject protected BarBazRule barBazRule;
					@Inject protected BarQuuxRule barQuuxRule;
					@Inject protected BarQuxListRule barQuxListRule;
				
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
								.setBarBarOne(barBarOneRule.evaluate(input));
							
							output
								.setBarBarTwo(barBarTwoRule.evaluate(input));
							
							output
								.getOrCreateBarBaz()
								.setBarBaz1(barBazRule.evaluate(input));
							
							output
								.setBarQuxList(barQuxListRule.evaluate(input));
							
							output
								.setBarQuux(barQuuxRule.evaluate(input));
							
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
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.reports.ReportFunction;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.BarReport.BarReportBuilder;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
				@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
				public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected BarBarOneRule barBarOneRule;
					@Inject protected BarBarTwoRule barBarTwoRule;
					@Inject protected BarBazRule barBazRule;
					@Inject protected BarQuuxRule barQuuxRule;
					@Inject protected BarQuxListRule barQuxListRule;
				
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
								.setBarBarOne(barBarOneRule.evaluate(input));
							
							output
								.setBarBarTwo(MapperC.of(barBarTwoRule.evaluate(input)).get());
							
							output
								.getOrCreateBarBaz()
								.setBarBaz1(barBazRule.evaluate(input));
							
							output
								.setBarQuxList(barQuxListRule.evaluate(input));
							
							output
								.setBarQuux(barQuuxRule.evaluate(input));
							
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
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.reports.ReportFunction;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.BarReport.BarReportBuilder;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
				@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
				public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected BarBarTwoRule barBarTwoRule;
					@Inject protected BarBazRule barBazRule;
					@Inject protected BarQuxListRule barQuxListRule;
					@Inject protected New_BarBarOneRule new_BarBarOneRule;
				
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
								.setBarBarOne(new_BarBarOneRule.evaluate(input));
							
							output
								.setBarBarTwo(barBarTwoRule.evaluate(input));
							
							output
								.getOrCreateBarBaz()
								.setBarBaz1(barBazRule.evaluate(input));
							
							output
								.setBarQuxList(barQuxListRule.evaluate(input));
							
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
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.reports.ReportFunction;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.BarReport.BarReportBuilder;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
				@ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
				public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected BarBarTwoRule barBarTwoRule;
					@Inject protected BarBazRule barBazRule;
					@Inject protected BarQuxListRule barQuxListRule;
				
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
								.setBarBarTwo(barBarTwoRule.evaluate(input));
							
							output
								.getOrCreateBarBaz()
								.setBarBaz1(barBazRule.evaluate(input));
							
							output
								.setBarQuxList(barQuxListRule.evaluate(input));
							
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
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.reports.ReportFunction;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.BarReport.BarReportBuilder;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
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
		code.compileToClasses
	}
	
	def loadRule(Map<String, Class<?>> classes, String ruleName) {
		val Class<?> ruleClass  = classes.get(ruleName)
		assertNotNull(ruleClass)
		val Injector injector = Guice.createInjector();
		return injector.getInstance(ruleClass)
	}

	@Test
	def void validPath() {
		val code = '''
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				baz string (1..1)
			
			reporting rule Rule1 from Foo:
				extract item->bar
				then extract item->baz
		'''.generateCode
		val ruleJava = code.get("com.rosetta.test.model.reports.Rule1Rule")
		try {
			assertThat(ruleJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import com.rosetta.test.model.Bar;
			import com.rosetta.test.model.Foo;
			
			
			@ImplementedBy(Rule1Rule.Rule1RuleDefault.class)
			public abstract class Rule1Rule implements ReportFunction<Foo, String> {
			
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
						final MapperS<Bar> thenArg = MapperS.of(input)
							.mapSingleToItem(item -> item.<Bar>map("getBar", foo -> foo.getBar()));
						output = thenArg
							.mapSingleToItem(item -> item.<String>map("getBaz", bar -> bar.getBaz())).get();
						
						return output;
					}
				}
			}
			'''
			assertEquals(expected, ruleJava)
			val classes = code.compileToClasses
			val ruleImpl = classes.loadRule("com.rosetta.test.model.reports.Rule1Rule")
			assertNotNull(ruleImpl)
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
		val rule = code.get("com.rosetta.test.model.reports.Rule1Rule")
		assertThat(rule, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.reports;
		
		import com.google.inject.ImplementedBy;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.reports.ReportFunction;
		import com.rosetta.test.model.Bar;
		import com.rosetta.test.model.Foo;
		import javax.inject.Inject;
		
		
		@ImplementedBy(Rule1Rule.Rule1RuleDefault.class)
		public abstract class Rule1Rule implements ReportFunction<Foo, String> {
			
			// RosettaFunction dependencies
			//
			@Inject protected Rule2Rule rule2Rule;
		
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
					final MapperS<Bar> thenArg = MapperS.of(rule2Rule.evaluate(input));
					output = thenArg
						.mapSingleToItem(item -> item.<String>map("getVal", bar -> bar.getVal())).get();
					
					return output;
				}
			}
		}
		'''
		assertEquals(expected, rule)
		val classes = code.compileToClasses
		val ruleImpl = classes.loadRule("com.rosetta.test.model.reports.Rule1Rule")
		assertNotNull(ruleImpl)
	}

	@Test
	def void filter() {
		val code = '''
			reporting rule SimpleRule from Input:
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter traderef="Hello"
			
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val ruleJava = code.get("com.rosetta.test.model.reports.SimpleRuleRule")
		assertThat(ruleJava, CoreMatchers.notNullValue())
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
		
		@ImplementedBy(SimpleRuleRule.SimpleRuleRuleDefault.class)
		public abstract class SimpleRuleRule implements ReportFunction<Input, Input> {
			
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
		
			public static class SimpleRuleRuleDefault extends SimpleRuleRule {
				@Override
				protected Input.InputBuilder doEvaluate(Input input) {
					Input.InputBuilder output = Input.builder();
					return assignOutput(output, input);
				}
				
				protected Input.InputBuilder assignOutput(Input.InputBuilder output, Input input) {
					output = toBuilder(MapperS.of(input)
						.filterSingleNullSafe(item -> areEqual(item.<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("Hello"), CardinalityOperator.All).get()).get());
					
					return Optional.ofNullable(output)
						.map(o -> o.prune())
						.orElse(null);
				}
			}
		}
		'''
		assertEquals(expected, ruleJava)
		code.compileToClasses
	}

	@Test
	def void filter2() {
		val code = '''
			reporting rule SimpleRule from Input:
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter traderef exists
									
			type Input:
				traderef string (0..1)
			
		'''.generateCode
		val ruleJava = code.get("com.rosetta.test.model.reports.SimpleRuleRule")
		assertThat(ruleJava, CoreMatchers.notNullValue())

		code.compileToClasses
	}

	@Test
	def void filterWhenRule() {
		val code = '''
			reporting rule TestRule from Input:
				extract item->flag
						
			reporting rule FilterRule from Input:
				filter TestRule then extract traderef
			
			type Input:
				traderef string (1..1)
				flag boolean (1..1)
			
		'''.generateCode
		val ruleJava = code.get("com.rosetta.test.model.reports.FilterRuleRule")
		assertThat(ruleJava, CoreMatchers.notNullValue())

		code.compileToClasses
	}

	@Test
	def void filterWhenCount() {
		val code = '''
			reporting rule IsFixedFloat from Foo:
				extract fixed count = 12
			
			type Foo:
				fixed string (0..*)
				floating string (0..*)
			
		'''.generateCode
		val ruleJava = code.get("com.rosetta.test.model.reports.IsFixedFloatRule")
		assertThat(ruleJava, CoreMatchers.notNullValue())
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
						.mapSingleToItem(item -> areEqual(MapperS.of(item.<String>mapC("getFixed", foo -> foo.getFixed()).resultCount()), MapperS.of(12), CardinalityOperator.All).asMapper()).get();
					
					return output;
				}
			}
		}
		'''
		code.compileToClasses
		assertEquals(expected, ruleJava)
	}

	@Test
	def void functionCallsWithLiteralInputFromExtract() {
		val code = ''' 
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
		code.compileToClasses
	}
	
	@Test
	def void shouldUseRuleFromDifferentNS() {
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
		val ruleImpl = classes.loadRule("ns2.reports.Rule2Rule")
		assertNotNull(ruleImpl)
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
		val code = '''
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
		code.compileToClasses
	}

	@Test
	def void longNestedIfElseWithNoReturn() {
		val code = '''
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
		code.compileToClasses
	}

	@Test
	def void ifWithSingleCardinality() {
		val code = '''
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
		code.compileToClasses
	}
	
	@Test
	def void ifWithMultipleCardinality() {
		val code = '''
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
		code.compileToClasses
	}

	@Test
	def void shouldGenerateDataType() {
		val code = '''
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
		code.compileToClasses
	}
	
	@Test
	def void shouldTypeResolutionForListOperation() {
		val code = '''
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
		code.compileToClasses
	}
	
	@Test
	def void shouldTypeResolutionForListOperation2() {
		val code = '''
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
		code.compileToClasses
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
					output = FooEnum.BAR;
					
					return output;
				}
			}
		}
		'''
		
		assertEquals(expectedRule, rule)
		
		code.compileToClasses
	}
}
