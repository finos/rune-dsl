package com.regnosys.rosetta.generator.java.reports

import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.rosetta.model.lib.records.Date
import com.rosetta.model.lib.reports.ReportFunction
import java.math.BigDecimal
import java.time.LocalTime
import java.time.ZonedDateTime
import org.junit.jupiter.api.Test
import com.rosetta.model.lib.ModelReportId
import com.rosetta.util.types.generated.GeneratedJavaClassService

@InjectWith(RosettaTestInjectorProvider)
@ExtendWith(InjectionExtension)
class ReportingTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension GeneratedJavaClassService
	@Inject extension TabulatorTestUtil
	
	@Test
	def void generateTabulatorWithOverlappingAttributeNamesInSubreports() {
		val model = '''
		body Authority Body
		corpus Act Corpus	
		
		report Body Corpus in real-time
		    from string
		    when Foo
		    with type Report
		
		eligibility rule Foo from string:
			True
		
		type Report:
			subreport1 Subreport1 (1..1)
			subreport2 Subreport2 (1..1)
		
		type Subreport1:
			value string (1..1)
				[ruleReference Bar]
		
		type Subreport2:
			value string (1..1)
				[ruleReference Bar]
		
		reporting rule Bar from string:
			item
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("com.rosetta.test.model"), "Body", "Corpus")
		val reportFunctionClass = reportId.toJavaReportFunction
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val reportFunction = classes.<ReportFunction<Object, RosettaModelObject>>createInstance(reportFunctionClass)
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val report = reportFunction.evaluate("input")
		
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		subreport1:
			value: input
		
		subreport2:
			value: input
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForHeroModel() {
		val model = '''
			namespace "test.reg"
			version "test"
			
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
			    powers PowerEnum (0..*)
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
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "5"  provision "Attribute - Int."]
			    extract attribute -> heroInt as "Attribute - Int"
			
			reporting rule AttributeNumber from Person: <"Attribute - Number">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "6"  provision "Attribute - Number."]
			    extract attribute -> heroNumber as "Attribute - Number"
			
			reporting rule AttributeZonedDateTime from Person: <"Attribute - ZonedDateTime">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "7"  provision "Attribute - ZonedDateTime."]
			    extract attribute -> heroZonedDateTime as "Attribute - ZonedDateTime"
			
			reporting rule AttributeTime from Person: <"Attribute - Time">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "8"  provision "Attribute - Time."]
			    extract attribute -> heroTime as "Attribute - Time"
			
			reporting rule HeroOrganisations from Person: <"Has Special Abilities">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "9"  provision "."]
			    extract organisations
			    then extract OrganisationReport {
			    	name: name,
			    	isGovernmentAgency: isGovernmentAgency,
			    	country: country
			    } as "Hero Organisations"
			
			reporting rule OrganisationName from Organisation: <"Organisation Name">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "10"  provision "."]
			    extract name as "Organisation Name"
			
			reporting rule OrganisationCountry from Organisation: <"Organisation Country">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "11"  provision "."]
			    extract country as "Organisation Country"
			
			reporting rule IsGovernmentAgency from Organisation: <"Is Government Agency">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "12"  provision "."]
			    extract isGovernmentAgency as "Is Government Agency"
			
			reporting rule NotModelled from Person: <"Not Modelled">
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "13"  provision "Not Modelled."]
			    "Not modelled" as "Not Modelled"
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("test.reg"), "Shield", "Avengers", "SokoviaAccords")
		val reportFunctionClass = reportId.toJavaReportFunction
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val reportFunction = classes.<ReportFunction<Object, RosettaModelObject>>createInstance(reportFunctionClass)
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val countryLoader = classes
			.get("test.reg.CountryEnum")
			.getDeclaredMethod("fromDisplayName", String)
		val powerLoader = classes
			.get("test.reg.PowerEnum")
			.getDeclaredMethod("fromDisplayName", String)
		
		val input = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "Person",
			#{
				"attribute" -> classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "Attribute",
					#{
						"heroInt" -> 9,
						"heroNumber" -> new BigDecimal("0.12345"),
						"heroTime" -> LocalTime.parse("10:13:07"),
						"heroZonedDateTime" -> ZonedDateTime.parse("2011-06-01T10:13:07Z")
					}
				),
				"dateOfBirth" -> Date.of(1970, 05, 29),
				"hasSpecialAbilities" -> true,
				"name" -> "Iron Man",
				"nationality" -> countryLoader.invoke(null, "UnitedStatesOfAmerica"),
				"organisations" -> #[
					classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "Organisation",
						#{
							"name" -> "Avengers",
							"isGovernmentAgency" -> false,
							"country" -> countryLoader.invoke(null, "UnitedStatesOfAmerica")
						}
					),
					classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "Organisation",
						#{
							"name" -> "S.H.I.E.L.D.",
							"isGovernmentAgency" -> true,
							"country" -> countryLoader.invoke(null, "UnitedStatesOfAmerica")
						}
					)
				],
				"powers" -> #["Armour", "Flight", "SuperhumanReflexes", "SuperhumanStrength"].map[powerLoader.invoke(null, it)]
			}
		)
		
		val report = reportFunction.evaluate(input)
		
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		heroName: Iron Man
		
		dateOfBirth: 1970-05-29
		
		nationality: UnitedStatesOfAmerica
		
		hasSpecialAbilities: true
		
		powers: [Armour, Flight, SuperhumanReflexes, SuperhumanStrength]
		
		attribute:
			heroInt: 9
			heroNumber: 0.12345
			heroTime: 10:13:07
			heroZonedDateTime: 2011-06-01T10:13:07Z
		
		organisations:
			name: Avengers
			isGovernmentAgency: false
			country: UnitedStatesOfAmerica
			name: S.H.I.E.L.D.
			isGovernmentAgency: true
			country: UnitedStatesOfAmerica
		
		notModelled: Not modelled
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateReportFromHierarchicalTypes() {
		val model = '''
			namespace "test.reg"
			version "test"
			
			type Input:
			    attr1 string (1..1)
			    attr2 string (1..1)
			    attr3 string (1..1)
			    eligible boolean (1..1)
			
			body Authority Auth
			
			corpus Act Corpus
			
			corpus Regulations Reg
			
			report Auth Corpus Reg in real-time
			    from Input
			    when IsEligible
			    with type Baz
			
			type Foo:
			    fooAttr string (1..1)
			        [ruleReference FooAttr]
			
			type Bar extends Foo:
			    barAttr string (1..1)
			        [ruleReference BarAttr]
			
			type Baz extends Bar:
			    bazAttr string (1..1)
			        [ruleReference BazAttr]
			
			eligibility rule IsEligible from Input:
			     filter eligible
			
			reporting rule FooAttr from Input:
			    extract attr1 as "Foo Attr"
			
			reporting rule BarAttr from Input:
			    extract attr2 as "Bar Attr"
			
			reporting rule BazAttr from Input:
			    extract attr3 as "Baz Attr"
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("test.reg"), "Auth", "Corpus", "Reg")
		val reportFunctionClass = reportId.toJavaReportFunction
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val reportFunction = classes.<ReportFunction<Object, RosettaModelObject>>createInstance(reportFunctionClass)
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val input = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "Input",
			#{
				"attr1" -> "A1",
				"attr2" -> "A2",
				"attr3" -> "A3",
				"eligible" -> true
			}
		)
		
		val report = reportFunction.evaluate(input)
		
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		fooAttr: A1
		
		barAttr: A2
		
		bazAttr: A3
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
}