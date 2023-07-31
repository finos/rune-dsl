package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Guice
import com.google.inject.Inject
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
						[legacy-syntax]
						extract repeatable Bar->quxList then
						(
							QuxQux1,
							QuxQux2
						) as "4 BarQuxList"

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
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Quux;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version 0.0.0
				 */
				public class TEST_REGMiFIRBlueprintReport<INKEY> implements Blueprint<Bar, Object, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public TEST_REGMiFIRBlueprintReport(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "TEST_REGMiFIR"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic3.rosetta#/0/@elements.2";
					}
					
					@Inject protected FooRuleRule fooRuleRef;
					@Inject protected BarBarOneRule barBarOneRef;
					@Inject protected BarBarTwoRule barBarTwoRef;
					@Inject protected BarBazRule barBazRef;
					@Inject protected BarQuuxRule barQuuxRef;
					@Inject protected BarQuxListRule barQuxListRef;
					
					@Override
					public BlueprintInstance<Bar, Object, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, getFooRule())
							.then(BlueprintBuilder.<Bar, Object, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, getBarBarOne()),
								startsWith(actionFactory, getBarBarTwo()),
								startsWith(actionFactory, getBarBaz()),
								startsWith(actionFactory, getBarQuux()),
								startsWith(actionFactory, getBarQuxList())
								)
							)
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					protected BlueprintInstance<Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarOne() {
						return barBarOneRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarTwo() {
						return barBarTwoRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBaz() {
						return barBazRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, Quux, INKEY, INKEY> getBarQuux() {
						return barQuuxRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarQuxList() {
						return barQuxListRef.blueprint();
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
				import java.util.Collection;
				
				
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
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Quux;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version 0.0.0
				 */
				public class TEST_REGMiFIRBlueprintReport<INKEY> implements Blueprint<Bar, Object, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public TEST_REGMiFIRBlueprintReport(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "TEST_REGMiFIR"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic3.rosetta#/0/@elements.2";
					}
					
					@Inject protected FooRuleRule fooRuleRef;
					@Inject protected BarBarOneRule barBarOneRef;
					@Inject protected BarBarTwoRule barBarTwoRef;
					@Inject protected BarBazRule barBazRef;
					@Inject protected BarQuuxRule barQuuxRef;
					@Inject protected BarQuxListRule barQuxListRef;
					
					@Override
					public BlueprintInstance<Bar, Object, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, getFooRule())
							.then(BlueprintBuilder.<Bar, Object, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, getBarBarOne()),
								startsWith(actionFactory, getBarBarTwo()),
								startsWith(actionFactory, getBarBaz()),
								startsWith(actionFactory, getBarQuux()),
								startsWith(actionFactory, getBarQuxList())
								)
							)
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					protected BlueprintInstance<Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarOne() {
						return barBarOneRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarTwo() {
						return barBarTwoRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBaz() {
						return barBazRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, Quux, INKEY, INKEY> getBarQuux() {
						return barQuuxRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarQuxList() {
						return barQuxListRef.blueprint();
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
				import java.util.Collection;
				
				
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
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.rosetta.test.model.Bar;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version 0.0.0
				 */
				public class TEST_REGMiFIRBlueprintReport<INKEY> implements Blueprint<Bar, String, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public TEST_REGMiFIRBlueprintReport(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "TEST_REGMiFIR"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic3.rosetta#/0/@elements.2";
					}
					
					@Inject protected FooRuleRule fooRuleRef;
					@Inject protected BarBarTwoRule barBarTwoRef;
					@Inject protected BarBazRule barBazRef;
					@Inject protected BarQuxListRule barQuxListRef;
					@Inject protected New_BarBarOneRule new_BarBarOneRef;
					
					@Override
					public BlueprintInstance<Bar, String, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, getFooRule())
							.then(BlueprintBuilder.<Bar, String, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, getBarBarTwo()),
								startsWith(actionFactory, getBarBaz()),
								startsWith(actionFactory, getBarQuxList()),
								startsWith(actionFactory, getNew_BarBarOne())
								)
							)
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					protected BlueprintInstance<Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarTwo() {
						return barBarTwoRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBaz() {
						return barBazRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarQuxList() {
						return barQuxListRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getNew_BarBarOne() {
						return new_BarBarOneRef.blueprint();
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
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.rosetta.test.model.Bar;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version 0.0.0
				 */
				public class TEST_REGMiFIRBlueprintReport<INKEY> implements Blueprint<Bar, String, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public TEST_REGMiFIRBlueprintReport(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "TEST_REGMiFIR"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic3.rosetta#/0/@elements.2";
					}
					
					@Inject protected FooRuleRule fooRuleRef;
					@Inject protected BarBarTwoRule barBarTwoRef;
					@Inject protected BarBazRule barBazRef;
					@Inject protected BarQuxListRule barQuxListRef;
					
					@Override
					public BlueprintInstance<Bar, String, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, getFooRule())
							.then(BlueprintBuilder.<Bar, String, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, getBarBarTwo()),
								startsWith(actionFactory, getBarBaz()),
								startsWith(actionFactory, getBarQuxList())
								)
							)
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					protected BlueprintInstance<Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBarTwo() {
						return barBarTwoRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarBaz() {
						return barBazRef.blueprint();
					}
					
					protected BlueprintInstance<Bar, String, INKEY, INKEY> getBarQuxList() {
						return barQuxListRef.blueprint();
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
				import java.util.Collection;
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
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.rosetta.test.model.Bar;
				import javax.inject.Inject;
				
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class TEST_REGMiFIRBlueprintReport<INKEY> implements Blueprint<Bar, Bar, INKEY, INKEY> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public TEST_REGMiFIRBlueprintReport(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "TEST_REGMiFIR"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#/0/@elements.2";
					}
					
					@Inject protected FooRuleRule fooRuleRef;
					
					@Override
					public BlueprintInstance<Bar, Bar, INKEY, INKEY> blueprint() {
						return 
							startsWith(actionFactory, getFooRule())
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					protected BlueprintInstance<Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
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
				import java.util.Collection;


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
			    [legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract repeatable Person -> organisations then
			    (
			        OrganisationName,
			        OrganisationCountry,
			        IsGovernmentAgency
			    ) as "Hero Organisations"
			
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
				import java.util.Collection;
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
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "validPath");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
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
							startsWith(actionFactory, actionFactory.<Foo, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#com.rosetta.test.model.Blueprint1", "item extract [item->bar] then [item extract [item->baz]]", null, foo -> MapperS.of(evaluate(foo))))
								.toBlueprint(getURI(), getName());
					}
					
					public String evaluate(Foo foo) {
						String string = doEvaluate(foo);
						return string;
					}
					
					private String doEvaluate(Foo foo) {
						String string = null;
						return assignOutput(string,foo);
					}
					
					private String assignOutput(String string, Foo foo) {
						string = MapperS.of(foo)
							.mapSingleToItem(item -> (MapperS<Bar>)item.<Bar>map("getBar", _foo -> _foo.getBar()))
							.apply(item -> item
								.mapSingleToItem(_item -> (MapperS<String>)_item.<String>map("getBaz", bar -> bar.getBaz()))).get();
					
						return string;
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
				Rule2 then
				extract val
			
			reporting rule Rule2 from Foo:
				extract bar
			
		'''.parseRosettaWithNoErrors
		val code = parsed.generateCode
		val bp = code.get("com.rosetta.test.model.blueprint.Rule1Rule")
		assertThat(bp, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Foo;
			import javax.inject.Inject;
			
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			/**
			 * @version test
			 */
			public class Rule1Rule<INKEY> implements Blueprint<Foo, String, INKEY, INKEY> {
				
				private final RosettaActionFactory actionFactory;
				
				@Inject
				public Rule1Rule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "Rule1"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.Rule1";
				}
				
				@Inject protected Rule2Rule rule2Ref;
				
				@Override
				public BlueprintInstance<Foo, String, INKEY, INKEY> blueprint() {
					return
						startsWith(actionFactory, actionFactory.<Foo, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#com.rosetta.test.model.Rule1", "Rule2 then [item extract [val]]", null, foo -> MapperS.of(evaluate(foo))))
							.toBlueprint(getURI(), getName());
				}
				
				public String evaluate(Foo foo) {
					String string = doEvaluate(foo);
					return string;
				}
				
				private String doEvaluate(Foo foo) {
					String string = null;
					return assignOutput(string,foo);
				}
				
				private String assignOutput(String string, Foo foo) {
					string = MapperS.of(rule2Ref.evaluate(MapperS.of(foo).get()))
						.apply(item -> item
							.mapSingleToItem(_item -> (MapperS<String>)_item.<String>map("getVal", bar -> bar.getVal()))).get();
				
					return string;
				}
			}
		'''
		assertEquals(expected, bp)
		val classes = code.compileToClasses
		val bpImpl = classes.loadBlueprint("com.rosetta.test.model.blueprint.Rule1Rule")
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
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.blueprint;
		
		import com.regnosys.rosetta.blueprints.Blueprint;
		import com.regnosys.rosetta.blueprints.BlueprintInstance;
		import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.functions.ModelObjectValidator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Input;
		import com.rosetta.test.model.Input.InputBuilder;
		import java.util.Optional;
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
			
			
			@Inject protected ModelObjectValidator objectValidator;
			
			@Override
			public BlueprintInstance<Input, Input, INKEY, INKEY> blueprint() {
				return
					startsWith(actionFactory, actionFactory.<Input, Input, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#com.rosetta.test.model.SimpleBlueprint", "item filter [traderef = \"Hello\"]", null, input -> MapperS.of(evaluate(input))))
						.toBlueprint(getURI(), getName());
			}
			
			public Input evaluate(Input input0) {
				Input.InputBuilder input1 = doEvaluate(input0);
				if (input1 != null) {
					objectValidator.validate(Input.class, input1);
				}
				return input1;
			}
			
			private Input.InputBuilder doEvaluate(Input input0) {
				Input.InputBuilder input1 = Input.builder();
				return assignOutput(input1,input0);
			}
			
			private Input.InputBuilder assignOutput(Input.InputBuilder input1, Input input0) {
				input1 = toBuilder(MapperS.of(input0)
					.filterSingleNullSafe(item -> (Boolean)areEqual(item.<String>map("getTraderef", input -> input.getTraderef()), MapperS.of("Hello"), CardinalityOperator.All).get()).get());
			
				return Optional.ofNullable(input1)
					.map(o -> o.prune())
					.orElse(null);
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
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
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
				extract Avengers -> heros
				then filter when rule CanWieldMjolnir
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
	def void filterWhenCount() {
		val blueprint = '''
			reporting rule IsFixedFloat from Foo:
				extract fixed count = 12
			
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
						startsWith(actionFactory, actionFactory.<Foo, Boolean, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#com.rosetta.test.model.IsFixedFloat", "item extract [fixed count = 12]", null, foo -> MapperS.of(evaluate(foo))))
							.toBlueprint(getURI(), getName());
				}
				
				public Boolean evaluate(Foo foo) {
					Boolean _boolean = doEvaluate(foo);
					return _boolean;
				}
				
				private Boolean doEvaluate(Foo foo) {
					Boolean _boolean = null;
					return assignOutput(_boolean,foo);
				}
				
				private Boolean assignOutput(Boolean _boolean, Foo foo) {
					_boolean = MapperS.of(foo)
						.mapSingleToItem(item -> (MapperS<Boolean>)areEqual(MapperS.of(item.<String>mapC("getFixed", _foo -> _foo.getFixed()).resultCount()), MapperS.of(Integer.valueOf(12)), CardinalityOperator.All).asMapper()).get();
				
					return _boolean;
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
		val bpImpl = classes.loadBlueprint("ns2.blueprint.Rule2Rule")
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
	def void shouldGenerateDataTypeThatExtendsAndOverrides() {
		val blueprint = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			from Bar
			when FooRule
			with type BarReport2
			
			eligibility rule FooRule from Bar:
				filter barA exists
			
			reporting rule Aa from Bar:
				extract barA as "A"

			reporting rule Aa2 from Bar:
				extract barA2 as "A"

			
			type Bar:
				barA string (0..1)
				barA2 string (0..1)

			
			type BarReport:
				aa string (1..1)
					[ruleReference Aa]
			
			type BarReport2 extends BarReport:
				aa string (1..1)
					[ruleReference Aa2]

		'''.generateCode
		blueprint.compileToClasses
		
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		
		val expected = '''
			@Override
				public BlueprintInstance<Bar, String, INKEY, INKEY> blueprint() {
					return 
						startsWith(actionFactory, getFooRule())
						.then(BlueprintBuilder.<Bar, String, INKEY, INKEY>or(actionFactory,
							startsWith(actionFactory, getAa2())
							)
						)
						.addDataItemReportBuilder(new BarReport2_DataItemReportBuilder())
						.toBlueprint(getURI(), getName());
				}
			'''
			assertTrue(blueprintJava.contains(expected), '''Expected string containing «expected» but was «blueprintJava»''')
	}
	
	
	@Test
	def void shouldGenerateDataTypeThatExtendsAndOverridesMultipleTypes() {
		val blueprint = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			from Bar
			when FooRule
			with type BarReport2
			
			eligibility rule FooRule from Bar:
				filter barA exists
			
			func FooFunc:
				inputs:
					bar Bar (1..1)
				output:
					result Bar (0..1)
				set result:
					bar filter barA exists
			
			reporting rule Aa from Bar:
				extract barA as "A"

			reporting rule Aa2 from Bar:
				extract barA2 as "A"
			
			reporting rule Bb from Bar:
				extract barB as "B"
				
			reporting rule Cc from Bar:
				extract barC as "C"

			
			type Bar:
				barA string (0..1)
				barA2 string (0..1)
				barB int (0..1)
				barC BazEnum (0..1)

			enum BazEnum:
				X
				Y
				Z
			
			type BarReport:
				aa string (1..1)
					[ruleReference Aa]
				bb int (1..1)
					[ruleReference Bb]
				cc BazEnum (1..1)
					[ruleReference Cc]
			
			type BarReport2 extends BarReport:
				aa string (1..1)
					[ruleReference Aa2]

		'''.generateCode
		blueprint.compileToClasses
		
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		
		val expected = '''
			@Override
				public BlueprintInstance<Bar, Object, INKEY, INKEY> blueprint() {
					return 
						startsWith(actionFactory, getFooRule())
						.then(BlueprintBuilder.<Bar, Object, INKEY, INKEY>or(actionFactory,
							startsWith(actionFactory, getAa2()),
							startsWith(actionFactory, getBb()),
							startsWith(actionFactory, getCc())
							)
						)
						.addDataItemReportBuilder(new BarReport2_DataItemReportBuilder())
						.toBlueprint(getURI(), getName());
				}'''
			assertTrue(blueprintJava.contains(expected), '''Expected string containing «expected» but was «blueprintJava»''')
			
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
		code.compileToClasses
	}
}
