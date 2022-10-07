package com.regnosys.rosetta.generator.java.blueprints

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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock

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
			eligibility rule ReportableTransation
			return "y"
		'''
		parseRosettaWithNoErrors(r)
	}

	@Test
	def void parseSimpleReportWithType() {
		val model = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			when FooRule
			with type BarReport
			
			eligibility rule FooRule
				filter when Bar->bar1 exists
			
			reporting rule BarBarOne
				extract Bar->bar1 as "1 BarOne"
			
			reporting rule BarBarTwo
				extract Bar->bar2 as "2 BarTwo"
			
			reporting rule BarBaz
				extract Bar->baz->baz1 as "3 BarBaz"
			
			reporting rule BarQuxList
				extract repeatable Bar->quxList then
				(
					QuxQux1,
					QuxQux2
				) as "4 BarQuxList"

			reporting rule QuxQux1
				extract Qux->qux1 as "5 QuxQux1"
			
			reporting rule QuxQux2
				extract Qux->qux2 as "6 QuxQux2"
			
			type Bar:
				bar1 string (1..1)
				bar2 string (0..*)
				baz Baz (1..1)
				quxList Qux (0..*)
				
			type Baz:
				 baz1 string (1..1)
			
			type Qux:
				 qux1 string (1..1)
				 qux2 string (1..1)
			
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
		'''
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.IdChange;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.blueprint.BarBarOneRule;
				import com.rosetta.test.model.blueprint.BarBarTwoRule;
				import com.rosetta.test.model.blueprint.BarBazRule;
				import com.rosetta.test.model.blueprint.BarQuxListRule;
				import com.rosetta.test.model.blueprint.FooRuleRule;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
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
						return "__synthetic1.rosetta#/0/@elements.2";
					}
					
					
					@Override
					public BlueprintInstance<Bar, String, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, getFooRule())
							.then(BlueprintBuilder.<Bar, String, INKEY, INKEY>or(actionFactory,
								startsWith(actionFactory, getBarBarOne()),
								startsWith(actionFactory, getBarBarTwo()),
								startsWith(actionFactory, getBarBaz()),
								startsWith(actionFactory, getBarQuxList())
								)
							)
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					@Inject private FooRuleRule fooRuleRef;
					protected BlueprintInstance <Bar, Bar, INKEY, INKEY> getFooRule() {
						return fooRuleRef.blueprint();
					}
					
					@Inject private BarBazRule barBazRef;
					protected BlueprintInstance <Bar, String, INKEY, INKEY> getBarBaz() {
						return barBazRef.blueprint();
					}
					
					@Inject private BarBarTwoRule barBarTwoRef;
					protected BlueprintInstance <Bar, String, INKEY, INKEY> getBarBarTwo() {
						return barBarTwoRef.blueprint();
					}
					
					@Inject private BarQuxListRule barQuxListRef;
					protected BlueprintInstance <Bar, String, INKEY, INKEY> getBarQuxList() {
						return barQuxListRef.blueprint();
					}
					
					@Inject private BarBarOneRule barBarOneRef;
					protected BlueprintInstance <Bar, String, INKEY, INKEY> getBarBarOne() {
						return barBarOneRef.blueprint();
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
				
				import java.util.Collection;
				
				import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
				import com.regnosys.rosetta.blueprints.DataItemReportUtils;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.test.model.BarReport;
				import com.rosetta.test.model.blueprint.BarBarOneRule;
				import com.rosetta.test.model.blueprint.BarBarTwoRule;
				import com.rosetta.test.model.blueprint.BarBazRule;
				import com.rosetta.test.model.blueprint.BarQuxListRule;
				import com.rosetta.test.model.blueprint.QuxQux1Rule;
				import com.rosetta.test.model.blueprint.QuxQux2Rule;
				
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
								if (BarBarOneRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarOne, String.class, data, BarBarOneRule.class);
								}
								if (BarBarTwoRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setBarBarTwo, String.class, data, BarBarTwoRule.class);
								}
								if (BarBazRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateBarBaz()::setBarBaz1, String.class, data, BarBazRule.class);
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
	def void parseSimpleReportWithDifferentNS() {
		val model = #['''
			namespace ns1
			
			type Bar:
				bar1 string (1..1)
				bar2 string (1..1)
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule BarBarTwo
				extract Bar->bar2 as "2 BarTwo"
			
		''','''
			namespace ns3
			
			import ns1.*
			import ns2.*
			
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
				when FooRule
				with type BarReport
			
			eligibility rule FooRule
				filter when Bar->bar1 exists
			
			reporting rule BarBarOne
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
				
				import java.util.Collection;
				
				import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
				import com.regnosys.rosetta.blueprints.DataItemReportUtils;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import ns2.blueprint.BarBarTwoRule;
				import ns3.BarReport;
				import ns3.blueprint.BarBarOneRule;
				
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
			when FooRule
			with type BarReport
			
			eligibility rule FooRule
				filter when Bar->bar1 exists
			
			type BarReport:
				barBarOne string (1..1)
					// no rules
			
			type Bar:
				bar1 string (1..1)

		'''
		val code = model.generateCode
		//println(code)
		val reportJava = code.get("com.rosetta.test.model.blueprint.TEST_REGMiFIRBlueprintReport")
		try {
			assertThat(reportJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.IdChange;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.blueprint.FooRuleRule;
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
					
					
					@Override
					public BlueprintInstance<Bar, Bar, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, getFooRule())
							.addDataItemReportBuilder(new BarReport_DataItemReportBuilder())
							.toBlueprint(getURI(), getName());
					}
					
					@Inject private FooRuleRule fooRuleRef;
					protected BlueprintInstance <Bar, Bar, INKEY, INKEY> getFooRule() {
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
				
				import java.util.Collection;
				
				import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
				import com.regnosys.rosetta.blueprints.DataItemReportUtils;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.test.model.BarReport;
				
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
			
			eligibility rule HasSuperPowers
			     filter when Person -> hasSpecialAbilities
			
			reporting rule HeroName <"Name">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "1" provision "Hero Name."]
			    extract Person -> name as "Hero Name"
			
			reporting rule DateOfBirth <"Date of birth">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Date of birth."]
			    extract Person -> dateOfBirth as "Date of Birth"
			
			reporting rule Nationality <"Nationality">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "2" provision "Nationality."]
			    extract Person -> nationality as "Nationality"
			
			reporting rule SpecialAbilities <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "3" provision "Has Special Abilities"]
			    extract Person -> hasSpecialAbilities as "Has Special Abilities"
			
			reporting rule Powers <"Super Power Name">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Powers."]
			    extract Person -> powers as "Powers"
			
			reporting rule AttributeInt <"Attribute - Int">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Int."]
			    extract Person -> attribute -> heroInt as "Attribute - Int"
			
			reporting rule AttributeNumber <"Attribute - Number">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Number."]
			    extract Person -> attribute -> heroNumber as "Attribute - Number"
			
			reporting rule AttributeZonedDateTime <"Attribute - ZonedDateTime">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - ZonedDateTime."]
			    extract Person -> attribute -> heroZonedDateTime as "Attribute - ZonedDateTime"
			
			reporting rule AttributeTime <"Attribute - Time">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "4"  provision "Attribute - Time."]
			    extract Person -> attribute -> heroTime as "Attribute - Time"
			
			reporting rule HeroOrganisations <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract repeatable Person -> organisations then
			    (
			        OrganisationName,
			        OrganisationCountry,
			        IsGovernmentAgency
			    ) as "Hero Organisations"
			
			reporting rule OrganisationName <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> name as "Organisation Name"
			
			reporting rule OrganisationCountry <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> country as "Organisation Country"
			
			reporting rule IsGovernmentAgency <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "."]
			    extract Organisation -> isGovernmentAgency as "Is Government Agency"
			
			reporting rule NotModelled <"Not Modelled">
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
				
				import com.rosetta.model.lib.records.Date;
				import java.math.BigDecimal;
				import java.time.LocalTime;
				import java.time.ZonedDateTime;
				import java.util.Collection;
				import test.reg.CountryEnum;
				import test.reg.PowerEnum;
				
				import com.regnosys.rosetta.blueprints.DataItemReportBuilder;
				import com.regnosys.rosetta.blueprints.DataItemReportUtils;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import test.reg.SokoviaAccordsReport;
				import test.reg.blueprint.AttributeIntRule;
				import test.reg.blueprint.AttributeNumberRule;
				import test.reg.blueprint.AttributeTimeRule;
				import test.reg.blueprint.AttributeZonedDateTimeRule;
				import test.reg.blueprint.DateOfBirthRule;
				import test.reg.blueprint.HeroNameRule;
				import test.reg.blueprint.HeroOrganisationsRule;
				import test.reg.blueprint.IsGovernmentAgencyRule;
				import test.reg.blueprint.NationalityRule;
				import test.reg.blueprint.NotModelledRule;
				import test.reg.blueprint.OrganisationCountryRule;
				import test.reg.blueprint.OrganisationNameRule;
				import test.reg.blueprint.PowersRule;
				import test.reg.blueprint.SpecialAbilitiesRule;
				
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
								if (HeroNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setHeroName, String.class, data, HeroNameRule.class);
								}
								if (DateOfBirthRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setDateOfBirth, Date.class, data, DateOfBirthRule.class);
								}
								if (NationalityRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setNationality, CountryEnum.class, data, NationalityRule.class);
								}
								if (SpecialAbilitiesRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setHasSpecialAbilities, Boolean.class, data, SpecialAbilitiesRule.class);
								}
								if (PowersRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setPowers, PowerEnum.class, data, PowersRule.class);
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
								if (OrganisationNameRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setName, String.class, data, OrganisationNameRule.class);
								}
								if (IsGovernmentAgencyRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setIsGovernmentAgency, Boolean.class, data, IsGovernmentAgencyRule.class);
								}
								if (OrganisationCountryRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder.getOrCreateOrganisations(ruleIdentifier.getRepeatableIndex().orElse(0))::setCountry, CountryEnum.class, data, OrganisationCountryRule.class);
								}
								if (NotModelledRule.class.isAssignableFrom(ruleType)) {
									DataItemReportUtils.setField(dataItemReportBuilder::setNotModelled, String.class, data, NotModelledRule.class);
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
			
			reporting rule Blueprint1
				extract Foo->bar then
				extract Bar->baz
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "validPath");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
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
			val classes = blueprint.compileToClasses
			val bpImpl = classes.loadBlueprint("com.rosetta.test.model.blueprint.Blueprint1Rule")
			assertNotNull(bpImpl)
			assertEquals(expected, blueprintJava)
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
			
			reporting rule Blueprint1
				extract Input->input2->name
		'''.parseRosetta.assertError(ROSETTA_FEATURE_CALL, RosettaIssueCodes.MISSING_ATTRIBUTE,
			"attempted to reference unknown field of Input2")
	}

	@Test
	def void brokenAndInputTypes() {
		'''
			reporting rule Blueprint1
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
			"Input type of Input2 is not assignable from type Input of previous node ")
	}
	
	@Test
	def void multiplicationExpression() {
		val model = '''
			reporting rule Blueprint1
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
			reporting rule Blueprint1
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
			reporting rule Blueprint1
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
			reporting rule Blueprint1
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
				
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.mapper.MapperS;
				import javax.inject.Inject;
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.Filter;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
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
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void numberOr() {
		val blueprint = '''
			reporting rule Blueprint1
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
				
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import java.math.BigDecimal;
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
								startsWith(actionFactory, actionFactory.<Input, BigDecimal, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#/0/@elements.0/@nodes/@node/@bps.1/@node", "Input->b", new RuleIdentifier("Input->b", getClass()), input -> MapperS.of(input).<BigDecimal>map("getB", _input -> _input.getB())))
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
	def void complexOr2() {
		val blueprint = '''
			reporting rule Blueprint1
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
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Input;
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
			reporting rule Blueprint1
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
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input2;
				import javax.inject.Inject;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input1;
				import com.rosetta.test.model.Input2;
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
			
			reporting rule Rule1
				Rule2 then
				extract Bar->val
			
			reporting rule Rule2
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
			reporting rule SimpleBlueprint 
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
		
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import javax.inject.Inject;
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		// manual imports
		import com.regnosys.rosetta.blueprints.Blueprint;
		import com.regnosys.rosetta.blueprints.BlueprintBuilder;
		import com.regnosys.rosetta.blueprints.BlueprintInstance;
		import com.regnosys.rosetta.blueprints.runner.actions.Filter;
		import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
		import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
		import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
		import com.rosetta.test.model.Input;
		import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
		
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
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	def void filter2() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter when Input->traderef exists
									
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter2");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void filterWhenRule() {
		val blueprint = '''
			reporting rule TestRule
				extract Input->flag
						
			reporting rule FilterRule
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
			reporting rule WorthyAvenger
				extract Avengers -> heros then 
				filter when rule CanWieldMjolnir 
					then filter when Hero -> name <> 'Thor'
					then extract Hero -> name
			
			eligibility rule CanWieldMjolnir
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
			
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Hero;
			import javax.inject.Inject;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			// manual imports
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Filter;
			import com.regnosys.rosetta.blueprints.runner.actions.FilterByRule;
			import com.regnosys.rosetta.blueprints.runner.actions.IdChange;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
			import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
			import com.rosetta.test.model.Avengers;
			import com.rosetta.test.model.Hero;
			import com.rosetta.test.model.blueprint.CanWieldMjolnirRule;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
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
				
				@Inject private CanWieldMjolnirRule canWieldMjolnirRef;
				protected BlueprintInstance <Hero, Boolean, INKEY, INKEY> getCanWieldMjolnir() {
					return canWieldMjolnirRef.blueprint();
				}
			}
		'''
		assertEquals(expected, blueprintJava)

	}
	
	@Test
	def void filterCardinalityAndType() {
		val model = '''
			reporting rule TestRule
				extract Input->flag
						
			reporting rule FilterRule
				filter when rule TestRule then extract Input->traderef
			
			
			type Input:
				traderef string (1..1)
				flag string (1..*)
			
		'''.parseRosetta
		
		model.assertError(BLUEPRINT_REF, RosettaIssueCodes.TYPE_ERROR,
			"output type of node String does not match required type of Boolean")
		model.assertError(BLUEPRINT_FILTER, null,
			"The expression for Filter must return a single value but the rule TestRule can return multiple values")
	}

	@Test
	def void filterWhenCount() {
		val blueprint = '''
			reporting rule IsFixedFloat
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
			
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import javax.inject.Inject;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			// manual imports
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
			import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
			import com.rosetta.test.model.Foo;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
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
			reporting rule FixedFloat
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
			reporting rule IsFixedFloat
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
			reporting rule IsFixedFloat
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
			reporting rule IsFixedFloat
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
			reporting rule IsFixedFloat
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
			
			reporting rule Rule1
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
			
			reporting rule Rule1
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
			
			reporting rule Rule1
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
			reporting rule FooRule
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
			
			reporting rule Rule1
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
			
			reporting rule Rule1
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
			
			reporting rule Rule1
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
			
			reporting rule Rule1
				extract TestObject -> fieldOne	
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2
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
			
			reporting rule Rule1
				extract TestObject -> fieldOne exists
			
		''','''
			namespace ns2
			
			import ns1.*
			
			reporting rule Rule2
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
			
			reporting rule Rule1
				extract TestObject -> fieldOne
							
			reporting rule Rule2
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
			
			reporting rule Rule1
				extract Foo->bar then
				Rule2
							
			reporting rule Rule2
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
		
		reporting rule BarField
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
		
		reporting rule BarField
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
		
		reporting rule BarField
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
		
		reporting rule BarField
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
		
		reporting rule BarField

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
			when FooRule
			with type BarReport
			
			eligibility rule FooRule
				filter when Bar->barA exists
			
			reporting rule Aa
				extract Bar->barA as "A"

			reporting rule Bb
				extract Bar->barB as "B"
				
			reporting rule Cc
				extract Bar->barC as "C"

			reporting rule Dd
				extract Bar->barD as "D"

			reporting rule Ee
				extract Bar->barE as "E"
				
			reporting rule Ff
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
	def void shouldGenerateDataTypeThatExtendsAndOverrides() {
		val blueprint = '''
			body Authority TEST_REG
			corpus TEST_REG MiFIR
			
			report TEST_REG MiFIR in T+1
			when FooRule
			with type BarReport2
			
			eligibility rule FooRule
				filter when Bar->barA exists
			
			reporting rule Aa
				extract Bar->barA as "A"

			reporting rule Aa2
				extract Bar->barA2 as "A"

			
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
			when FooRule
			with type BarReport2
			
			eligibility rule FooRule
				filter when Bar->barA exists
			
			reporting rule Aa
				extract Bar->barA as "A"

			reporting rule Aa2
				extract Bar->barA2 as "A"
			
			reporting rule Bb
				extract Bar->barB as "B"
				
			reporting rule Cc
				extract Bar->barC as "C"

			
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
		
		reporting rule FooRule
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
		
		reporting rule FooRule
			extract Foo -> bar 
				map [ item -> baz ] then
			extract Baz -> attr
		
		'''.generateCode
		blueprint.compileToClasses
	}

	@Test
	@Disabled
	def void getNestedMappings() {
		val blueprint = '''
			blueprint NotSoSimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input
			}
			
			type Input:
				child Child (1..1)
			
			type Child:
				traderef string (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #29" provision "" reportedField]
				tradeid int (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #30" provision ""]
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #31" provision "" reportedField]
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.NotSoSimpleBlueprint")
		// writeOutClasses(blueprint, "getNestedMappings");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Child;
				import com.rosetta.test.model.Input;
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class NotSoSimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
					@Override
					public String getName() {
						return "NotSoSimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#NotSoSimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
							.toBlueprint(getURI(), getName());
					}
					
					protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
						return Blueprint.of(
						new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS),
						new MappingGroup<>("annex 1 table2 #29", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_29_MAPPINGS),
						new MappingGroup<>("annex 1 table2 #31", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_31_MAPPINGS));
					}
					
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTraderef", Child::getTraderef)
					);
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_29_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTraderef", Child::getTraderef)
					);
					private static final List<Function<Input, Mapper<Integer>>> ANNEX_1_TABLE2_31_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTradeid", Child::getTradeid)
					);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}

	}

	@Test
	@Disabled
	def void getNestedMappingsWithMultiplePaths() {
		val blueprint = '''
			blueprint NotSoSimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input
			}
			
			type Input:
				childA Child (1..1)
				childB Child (1..1)
			
			type Child:
				traderef string (1..1)
					[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.NotSoSimpleBlueprint")
		// writeOutClasses(blueprint, "getNestedMappingsWithMultiplePaths");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Child;
				import com.rosetta.test.model.Input;
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class NotSoSimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
					@Override
					public String getName() {
						return "NotSoSimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#NotSoSimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
							.toBlueprint(getURI(), getName());
					}
					
					protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
						return Blueprint.of(
						new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
					}
					
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChildA", Input::getChildA).map("getTraderef", Child::getTraderef),
						i -> MapperS.of(i).map("getChildB", Input::getChildB).map("getTraderef", Child::getTraderef)
					);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}

	}

	@Test
	@Disabled
	def void genMappingCollection() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input then
				merge output Output 
			}
			
			type Input:
				traderefs string (1..*)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Output:
				transactionId string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
				transactionNumber number (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #29" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Merger;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.rosetta.model.lib.functions.Converter;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.MappingGroup;
			import com.rosetta.test.model.Input;
			import com.rosetta.test.model.Output;
			import java.math.BigDecimal;
			import java.util.Collection;
			import java.util.HashMap;
			import java.util.List;
			import java.util.Map;
			import java.util.function.BiConsumer;
			import java.util.function.Function;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Output, INKEY, INKEY> {
				@Override
				public String getName() {
					return "SimpleBlueprint"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#SimpleBlueprint";
				}
				
				
				@Override
				public BlueprintInstance<Input, Output, INKEY, INKEY> blueprint() { 
					return 
						startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
						.then(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
											new StringIdentifier("Output"), false))
						.toBlueprint(getURI(), getName());
				}
				
				protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
					return Blueprint.of(
					new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
				}
				
				protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> mergeOutput();
				
				protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> simpleMergeOutput() {
					Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> result = new HashMap<>();
					result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionId(Converter.convert(String.class, input)));
					result.put(new RosettaIdentifier("annex 1 table2 #29"), (builder, input) -> builder.setTransactionNumber(Converter.convert(BigDecimal.class, input)));
					result.put(new StringIdentifier("transactionId"), (builder, input) -> builder.setTransactionId(Converter.convert(String.class, input)));
					result.put(new StringIdentifier("transactionNumber"), (builder, input) -> builder.setTransactionNumber(Converter.convert(BigDecimal.class, input)));
					return result;
				}
				
				private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
					i -> MapperS.of(i).mapC("getTraderefs", Input::getTraderefs)
				);
				
				protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
			}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	@Disabled
	def void genMappingExtension() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input2 then
				merge output Output
			}
						
			type Input:
				traderef string (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Input2 extends Input:
			 	first string (1..1)
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Merger;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.rosetta.model.lib.functions.Converter;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.MappingGroup;
			import com.rosetta.test.model.Input;
			import com.rosetta.test.model.Input2;
			import com.rosetta.test.model.Output;
			import java.util.Collection;
			import java.util.HashMap;
			import java.util.List;
			import java.util.Map;
			import java.util.function.BiConsumer;
			import java.util.function.Function;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input2, Output, INKEY, INKEY> {
				@Override
				public String getName() {
					return "SimpleBlueprint"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#SimpleBlueprint";
				}
				
				
				@Override
				public BlueprintInstance<Input2, Output, INKEY, INKEY> blueprint() { 
					return 
						startsWith(BlueprintBuilder.<Input2, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput2", simpleMappingsInput2()))
						.then(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
											new StringIdentifier("Output"), false))
						.toBlueprint(getURI(), getName());
				}
				
				protected Collection<MappingGroup<Input2, ?>> simpleMappingsInput2() {
					return Blueprint.of(
					new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
				}
				
				protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> mergeOutput();
				
				protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> simpleMergeOutput() {
					Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> result = new HashMap<>();
					result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
					result.put(new StringIdentifier("transactionReferenceNumber"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
					return result;
				}
				
				private static final List<Function<Input2, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
					i -> MapperS.of(i).map("getTraderef", Input::getTraderef)
				);
				
				protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
			}
		'''
		// writeOutClasses(blueprint);
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	@Disabled
	def void genNestedBlueprints() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				( extract Input->traderef , extract Input->input2->colour)
				then Blueprint2
			
			reporting rule Blueprint2
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				merge output Output
						
			type Input:
				traderef string (1..1)
				input2 Input2 (1..1)
			
			type Input2:
				colour string (1..1)
			
			type Output:
				transactionReferenceNumber string (1..1)
				colour string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "genNestedBlueprints");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.test.model.Input;
				import com.rosetta.test.model.Input2;
				import com.rosetta.test.model.Output;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class Blueprint1<INKEY> implements Blueprint<Input, Output, INKEY, INKEY> {
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Output, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, String, INKEY, INKEY>or(
								startsWith(getRosettaActionFactory().<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->traderef", new StringIdentifier("->traderef"), input -> MapperS.of(input).map("getTraderef", Input::getTraderef))),
								startsWith(getRosettaActionFactory().<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->input2->colour", new StringIdentifier("->input2->colour"), input -> MapperS.of(input).map("getInput2", Input::getInput2).map("getColour", Input2::getColour)))
								)
							)
							.then(getBlueprint2())
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract BlueprintInstance<String, Output, INKEY, INKEY> getBlueprint2();
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void ingest() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				bpsource ISource <string, string> then
				ingest to Input
			}
						
			type Input:
				traderef string (1..1)
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "ingest");
		blueprint.compileToClasses
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint implements Blueprint<Void, Input, String, String> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Void, Input, String, String> blueprint() { 
						return 
							startsWith(getISource())
							.then(getRosettaActionFactory().<Input, String>newRosettaIngester("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Map to Rosetta", Input.class))
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract SourceNode<String, String> getISource();
				}
			'''
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void validate() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				validate as Input
			}
									
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "validate");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Input, INKEY, INKEY> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Input, INKEY, INKEY> blueprint() { 
						return 
							startsWith(getRosettaActionFactory().<Input, INKEY>newRosettaValidator("__synthetic1.rosetta#//@elements.0/@nodes/@node", "Validate Rosetta", Input.class))
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
	@Disabled
	def void source() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				bpsource asource <string, string>
			}
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "source");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint implements Blueprint<Void, String, String, String> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Void, String, String, String> blueprint() { 
						return 
							startsWith(getAsource())
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract SourceNode<String, String> getAsource();
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void calculationUnifiedType() {
		val blueprint = '''
			calculation Calc {
				outputVal int : arg1 + arg2
				outputDt number : arg3 + arg4
			}
			
			arguments Calc CalcArgs{
				arg1 int : is Input->val1
				arg2 int : is Input->val2
				arg3 number : is Input->val3
				arg4 number : is Input->val4
			}			
						
			type Input:
				val1 int (1..1)
				val2 int (1..1)
				val3 number (1..1)
				val4 number (1..1)
			
			
			blueprint Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				calculate calc Calc args CalcArgs
			}
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculationUnifiedType");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.CalculationFunction;
				import com.rosetta.model.lib.functions.CalculationFunction.CalculationArgFunctions;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Input;
				import java.math.BigDecimal;
				import java.util.Collection;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				import static com.rosetta.model.lib.functions.MapperMaths.*;
				import static com.rosetta.model.lib.validation.ValidatorHelper.*;
				
				public abstract class Blueprint1<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Number, INKEY, INKEY> {
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
							startsWith(BlueprintBuilder.<Input, Number, INKEY>doCalcMappings("__synthetic1.rosetta#//@elements.3/@nodes/@node", calcMappingsCalc()))
							.toBlueprint(getURI(), getName());
					}
					
					private Collection<MappingGroup<Input, ? extends Number>> calcMappingsCalc() {
						return Blueprint.of(
							new MappingGroup<Input, Integer>("outputVal", "__synthetic1.rosetta#Calc.outputVal", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg1"), args.get("arg2")), calcArgsCalc()))),
							
							new MappingGroup<Input, BigDecimal>("outputDt", "__synthetic1.rosetta#Calc.outputDt", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg3"), args.get("arg4")), calcArgsCalc())))
						);
					}
					
					private CalculationArgFunctions<Input> calcArgsCalc() {
						CalculationArgFunctions<Input> result = new CalculationArgFunctions<>();
						result.put("arg1", input->MapperS.of(input).map("getVal1", Input::getVal1));
						result.put("arg2", input->MapperS.of(input).map("getVal2", Input::getVal2));
						result.put("arg3", input->MapperS.of(input).map("getVal3", Input::getVal3));
						result.put("arg4", input->MapperS.of(input).map("getVal4", Input::getVal4));
						return result;
					}
				}
			'''
			assertEquals(expected, blueprintJava)

		} finally {
		}
	}

	@Test
	@Disabled
	def void calculation() {
		val blueprint = '''
			calculation Calc {
				outputVal int : arg1 + arg2
				outputDt string : arg3 + arg4
			}
			
			arguments Calc CalcArgs{
				arg1 int : is Input->val1
				arg2 int : is Input->val2
				arg3 date : is Input->val3
				arg4 time : is Input->val4
			}			
						
			type Input:
				val1 int (1..1)
				val2 int (1..1)
				val3 date (1..1)
				val4 time (1..1)
			
			
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				calculate calc Calc args CalcArgs
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculation");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.CalculationFunction;
				import com.rosetta.model.lib.functions.CalculationFunction.CalculationArgFunctions;
				import com.rosetta.test.model.Input;
				import java.time.LocalDate;
				import java.time.LocalTime;
				import java.util.Collection;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				import static com.rosetta.model.lib.functions.MapperMaths.*;
				
				public abstract class Blueprint1<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
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
							startsWith(BlueprintBuilder.<Input, Object, INKEY>doCalcMappings("__synthetic1.rosetta#//@elements.3/@nodes/@node", calcMappingsCalc()))
							.toBlueprint(getURI(), getName());
					}
					
					private Collection<MappingGroup<Input, ? extends Object>> calcMappingsCalc() {
						return Blueprint.of(
							new MappingGroup<Input, Integer>("outputVal", "__synthetic1.rosetta#Calc.outputVal", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg1"), args.get("arg2")), calcArgsCalc()))),
							
							new MappingGroup<Input, String>("outputDt", "__synthetic1.rosetta#Calc.outputDt", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg3"), args.get("arg4")), calcArgsCalc())))
						);
					}
					
					private CalculationArgFunctions<Input> calcArgsCalc() {
						CalculationArgFunctions<Input> result = new CalculationArgFunctions<>();
						result.put("arg1", input->MapperS.of(input).map("getVal1", Input::getVal1));
						result.put("arg2", input->MapperS.of(input).map("getVal2", Input::getVal2));
						result.put("arg3", input->MapperS.of(input).map("getVal3", Input::getVal3));
						result.put("arg4", input->MapperS.of(input).map("getVal4", Input::getVal4));
						return result;
					}
				}
			'''
			assertEquals(expected, blueprintJava)

		} finally {
		}
	}

	@Disabled
	@Test
	def void calculationInline() {
		val blueprint = '''
			
			blueprint Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				calculate calc quantity string : "1" args a1 int : is Input->val1
			}
			
			type Input:
				val1 int (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculationInline");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses

		} finally {
		}
	}
}
