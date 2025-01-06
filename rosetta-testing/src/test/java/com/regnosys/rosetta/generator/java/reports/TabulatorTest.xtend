package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.ModelReportId
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.util.DottedPath
import com.rosetta.util.types.generated.GeneratedJavaClass
import com.rosetta.util.types.generated.GeneratedJavaClassService
import java.math.BigDecimal
import java.net.URL
import java.util.Map
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*

import static extension com.regnosys.rosetta.tests.util.CustomConfigTestHelper.*
import com.rosetta.model.metafields.MetaFields

@InjectWith(RosettaTestInjectorProvider)
@ExtendWith(InjectionExtension)
class TabulatorTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension GeneratedJavaClassService
	@Inject extension TabulatorTestUtil
	
	final StringConcatenationClient HEADER = '''
	namespace com.rosetta.test.model
	
	body Authority TEST_REG
	corpus TEST_REG Corp

	report TEST_REG Corp in T+1
	from ReportableInput
	when IsReportableInput
	with type Report
	
	eligibility rule IsReportableInput from ReportableInput:
		filter True
	
	annotation projection:
	'''
	
	@Test
	def void generateTabulatorForReportWithoutLists() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				title string (1..1)
			
			type Report:
				basic string (1..1)
					[ruleReference Basic]
				basicNonReported int (0..1)
				subreport Subreport (1..1)
				subreportWithRule Subreport (1..1)
					[ruleReference SubreportWithRule]
			
			type Subreport:
				subbasic string (1..1)
					[ruleReference Subbasic]
			
			reporting rule Basic from ReportableInput:
				extract title
			
			reporting rule Subbasic from ReportableInput:
				extract title + " - sub"
			
			reporting rule SubreportWithRule from ReportableInput:
				Subreport { subbasic: Subbasic + " - created" }
					as "Subreport from a rule"
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val reportTabulatorClass = reportId.toJavaReportTabulator
		
		val reportTabulatorCode = code.get(reportTabulatorClass.canonicalName.withDots)
		assertThat(reportTabulatorCode, CoreMatchers.notNullValue())
		var expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.test.model.Report;
			import java.util.List;
			import javax.inject.Inject;
			import javax.inject.Singleton;
			
			
			@ImplementedBy(TEST_REGCorpReportTabulator.Impl.class)
			public interface TEST_REGCorpReportTabulator extends Tabulator<Report> {
				@Singleton
				class Impl implements TEST_REGCorpReportTabulator {
					private final ReportTypeTabulator tabulator;
			
					@Inject
					public Impl(ReportTypeTabulator tabulator) {
						this.tabulator = tabulator;
					}
			
					@Override
					public List<FieldValue> tabulate(Report input) {
						return tabulator.tabulate(input);
					}
				}
			}
		'''
		assertEquals(expected, reportTabulatorCode)
		
		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTypeTabulator", Tabulator)
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.ModelSymbolId;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl;
			import com.rosetta.model.lib.reports.Tabulator.NestedFieldValueImpl;
			import com.rosetta.test.model.Report;
			import com.rosetta.util.DottedPath;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;
			import javax.inject.Inject;
			import javax.inject.Singleton;
			
			
			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
				@Singleton
				class Impl implements ReportTypeTabulator {
					private final Field basicField;
					private final Field subreportField;
					private final Field subreportWithRuleField;

					private final SubreportTypeTabulator subreportTypeTabulator;
			
					@Inject
					public Impl(SubreportTypeTabulator subreportTypeTabulator) {
						this.subreportTypeTabulator = subreportTypeTabulator;
						this.basicField = new FieldImpl(
							"basic",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "Basic")),
							Optional.empty(),
							Arrays.asList()
						);
						this.subreportField = new FieldImpl(
							"subreport",
							false,
							Optional.empty(),
							Optional.empty(),
							Arrays.asList()
						);
						this.subreportWithRuleField = new FieldImpl(
							"subreportWithRule",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "SubreportWithRule")),
							Optional.of("Subreport from a rule"),
							Arrays.asList()
						);
					}

					@Override
					public List<FieldValue> tabulate(Report input) {
						FieldValue basic = new FieldValueImpl(basicField, Optional.ofNullable(input.getBasic()));
						FieldValue subreport = Optional.ofNullable(input.getSubreport())
							.map(x -> new NestedFieldValueImpl(subreportField, Optional.of(subreportTypeTabulator.tabulate(x))))
							.orElse(new NestedFieldValueImpl(subreportField, Optional.empty()));
						FieldValue subreportWithRule = Optional.ofNullable(input.getSubreportWithRule())
							.map(x -> new NestedFieldValueImpl(subreportWithRuleField, Optional.of(subreportTypeTabulator.tabulate(x))))
							.orElse(new NestedFieldValueImpl(subreportWithRuleField, Optional.empty()));
						return Arrays.asList(
							basic,
							subreport,
							subreportWithRule
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val subreport1 = classes.createInstanceUsingBuilder("Subreport", #{"subbasic" -> "My reportable input - sub"})
		val subreport2 = classes.createInstanceUsingBuilder("Subreport", #{"subbasic" -> "My reportable input - created"})
		val report = classes.createInstanceUsingBuilder("Report", #{"basic" -> "My reportable input", "subreport" -> subreport1, "subreportWithRule" -> subreport2})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		basic: My reportable input
		
		subreport:
			subbasic: My reportable input - sub
		
		subreportWithRule:
			subbasic: My reportable input - created
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForReportWithLists() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				title string (1..1)
				values int (0..*)
			
			type Report:
				basicList int (0..*)
					[ruleReference BasicList]
				subreportList Subreport (0..*)
					[ruleReference SubreportList]
			
			type Subreport:
				subbasic string (1..1)
					[ruleReference Subbasic]
				n int (0..1)
					[ruleReference N]
			
			reporting rule BasicList from ReportableInput:
				extract values
			
			reporting rule Subbasic from ReportableInput:
				extract title + " - sub"
					as "Basic value of subreport"
			
			reporting rule N from ReportableInput:
				extract values first
			
			reporting rule SubreportList from ReportableInput:
				extract reportableInput [
					reportableInput
						extract values
						then extract Subbasic(reportableInput) + item to-string
						then extract Subreport { subbasic: item, n: N(reportableInput)}
				]
					as "Subreport group"
		'''
		val code = model.generateCode

		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTypeTabulator", Tabulator)
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.ModelSymbolId;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl;
			import com.rosetta.model.lib.reports.Tabulator.MultiNestedFieldValueImpl;
			import com.rosetta.test.model.Report;
			import com.rosetta.util.DottedPath;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;
			import java.util.stream.Collectors;
			import javax.inject.Inject;
			import javax.inject.Singleton;
			
			
			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
				@Singleton
				class Impl implements ReportTypeTabulator {
					private final Field basicListField;
					private final Field subreportListField;

					private final SubreportTypeTabulator subreportTypeTabulator;

					@Inject
					public Impl(SubreportTypeTabulator subreportTypeTabulator) {
						this.subreportTypeTabulator = subreportTypeTabulator;
						this.basicListField = new FieldImpl(
							"basicList",
							true,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "BasicList")),
							Optional.empty(),
							Arrays.asList()
						);
						this.subreportListField = new FieldImpl(
							"subreportList",
							true,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "SubreportList")),
							Optional.of("Subreport group"),
							Arrays.asList()
						);
					}

					@Override
					public List<FieldValue> tabulate(Report input) {
						FieldValue basicList = new FieldValueImpl(basicListField, Optional.ofNullable(input.getBasicList()));
						FieldValue subreportList = Optional.ofNullable(input.getSubreportList())
							.map(x -> x.stream()
								.map(_x -> subreportTypeTabulator.tabulate(_x))
								.collect(Collectors.toList()))
							.map(fieldValues -> new MultiNestedFieldValueImpl(subreportListField, Optional.of(fieldValues)))
							.orElse(new MultiNestedFieldValueImpl(subreportListField, Optional.empty()));
						return Arrays.asList(
							basicList,
							subreportList
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val subreport1 = classes.createInstanceUsingBuilder("Subreport", #{"subbasic" -> "My reportable input 1 - sub"})
		val subreport2 = classes.createInstanceUsingBuilder("Subreport", #{"subbasic" -> "My reportable input 2 - sub", "n" -> 42})
		val report = classes.createInstanceUsingBuilder("Report", #{"basicList" -> #["My reportable input 1", "My reportable input 2"], "subreportList" -> #[subreport1, subreport2]})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		basicList: [My reportable input 1, My reportable input 2]
		
		subreportList:
			subbasic: My reportable input 1 - sub
			n: <empty>
			subbasic: My reportable input 2 - sub
			n: 42
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForReportWithDeeplyNestedLists() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				titles string (0..*)
				values int (0..*)
			
			type Report:
				subreportList Subreport (0..*)
					[ruleReference SubreportList]
			
			type Subreport:
				subsubreportList Subsubreport (0..*)
					[ruleReference SubsubreportList]
			
			type Subsubreport:
				subsubbasic string (1..1)
					[ruleReference Subsubbasic]
			
			reporting rule Subsubbasic from string:
				extract item
					as "Basic value of subsubreport"
			
			reporting rule SubsubreportList from string:
				extract Subsubreport { subsubbasic: item }
					as "List of subsubreports"
			
			reporting rule SubreportList from ReportableInput:
				extract reportableInput [
					reportableInput
						extract values
						then extract v [
							reportableInput
								extract titles
								then extract item + v to-string
								then extract SubsubreportList
								then Subreport { subsubreportList: item }
						]
				]
					as "Subreport group"
		'''
		val code = model.generateCode

		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTypeTabulator", Tabulator)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val subsubreport1 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "1"})
		val subsubreport2 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "2"})
		val subsubreport3 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "3"})
		val subreport1 = classes.createInstanceUsingBuilder("Subreport", #{"subsubreportList" -> #[subsubreport1, subsubreport2]})
		val subreport2 = classes.createInstanceUsingBuilder("Subreport", #{})
		val subreport3 = classes.createInstanceUsingBuilder("Subreport", #{"subsubreportList" -> #[subsubreport3]})
		val report = classes.createInstanceUsingBuilder("Report", #{"subreportList" -> #[subreport1, subreport2, subreport3]})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		subreportList:
			subsubreportList:
				subsubbasic: 1
				subsubbasic: 2
			subsubreportList: <empty>
			subsubreportList:
				subsubbasic: 3
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForExtendedReport() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				title string (1..1)
				value int (1..1)
			
			type BaseReport:
				basic1 string (1..1)
					[ruleReference Basic1]
				basic2 int (1..1)
					[ruleReference Basic2]
			
			type Report extends BaseReport:
				override basic1 string (1..1)
				basic2 int (1..1)
					[ruleReference Basic2Modified]
				basic3 number (1..1)
					[ruleReference Basic3]
			
			reporting rule Basic1 from ReportableInput:
				extract title
			
			reporting rule Basic2 from ReportableInput:
				extract value
					as "Basic 2 Rule"
			
			reporting rule Basic2Modified from ReportableInput:
				extract value*2
					as "Basic 2 Rule - Modified"
			
			reporting rule Basic3 from ReportableInput:
				extract value/2
		'''
		val code = model.generateCode
		
		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTypeTabulator", Tabulator)
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.ModelSymbolId;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl;
			import com.rosetta.test.model.Report;
			import com.rosetta.util.DottedPath;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;
			import javax.inject.Singleton;
			
			
			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
				@Singleton
				class Impl implements ReportTypeTabulator {
					private final Field basic1Field;
					private final Field basic2Field;
					private final Field basic3Field;

					public Impl() {
						this.basic1Field = new FieldImpl(
							"basic1",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "Basic1")),
							Optional.empty(),
							Arrays.asList()
						);
						this.basic2Field = new FieldImpl(
							"basic2",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "Basic2Modified")),
							Optional.of("Basic 2 Rule - Modified"),
							Arrays.asList()
						);
						this.basic3Field = new FieldImpl(
							"basic3",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "Basic3")),
							Optional.empty(),
							Arrays.asList()
						);
					}

					@Override
					public List<FieldValue> tabulate(Report input) {
						FieldValue basic1 = new FieldValueImpl(basic1Field, Optional.ofNullable(input.getBasic1()));
						FieldValue basic2 = new FieldValueImpl(basic2Field, Optional.ofNullable(input.getBasic2()));
						FieldValue basic3 = new FieldValueImpl(basic3Field, Optional.ofNullable(input.getBasic3()));
						return Arrays.asList(
							basic1,
							basic2,
							basic3
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val report = classes.createInstanceUsingBuilder("Report", #{"basic1" -> "My reportable input", "basic2" -> 42, "basic3" -> new BigDecimal("10.5")})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		basic1: My reportable input
		
		basic2: 42
		
		basic3: 10.5
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorWithExternalRules() {
		val model = '''
			namespace com.rosetta.test.model
			
			body Authority TEST_REG
			corpus TEST_REG Corp
			
			report TEST_REG Corp in T+1
			from ReportableInput
			when IsReportableInput
			with type Report
			with source RuleSource
			
			eligibility rule IsReportableInput from ReportableInput:
				filter True
			
			type ReportableInput:
				title string (1..1)
				value int (1..1)
			
			type BaseReport:
				basic1 string (1..1)
					[ruleReference Basic1]
				basic2 int (1..1)
			
			type Report extends BaseReport:
				override basic1 string (1..1)
				basic2 int (1..1)
				basic3 number (1..1)
			
			reporting rule Basic1 from ReportableInput:
				extract title
					as "Inline"
			
			reporting rule Basic1InSource from ReportableInput:
				extract title
					as "In source"
			
			reporting rule Basic1InSourceModified from ReportableInput:
				extract title
					as "In source modified"
			
			reporting rule Basic2 from ReportableInput:
				extract value
					as "Basic 2 Rule"
			
			reporting rule Basic2Modified from ReportableInput:
				extract value*2
					as "Basic 2 Rule modified"
			
			reporting rule Basic3 from ReportableInput:
				extract value/2
			
			rule source BaseRuleSource {
				BaseReport:
				+ basic1
					[ruleReference Basic1InSource]
				+ basic2
					[ruleReference Basic2]
				
				Report:
				+ basic2
					[ruleReference Basic2]
			}
			
			rule source RuleSource extends BaseRuleSource {
				BaseReport:
				+ basic1
					[ruleReference Basic1InSourceModified]
				
				Report:
				- basic2
				+ basic2
					[ruleReference Basic2Modified]
				+ basic3
					[ruleReference Basic3]
			}
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val report = classes.createInstanceUsingBuilder("Report", #{"basic1" -> "My reportable input", "basic2" -> 42, "basic3" -> new BigDecimal("10.5")})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		basic1: My reportable input
		
		basic2: 42
		
		basic3: 10.5
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForNonReportableType() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				title string (1..1)
			
			type Report:
				basicNonReported int (0..1)
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val expectedFields =
		'''
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
		val report = classes.createInstanceUsingBuilder("Report", #{})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void generateTabulatorForProjectionWithDeeplyNestedLists() {
		val model = '''
			«HEADER»
			
			func Projection:
				[projection]
				inputs:
					input ReportableInput (1..1)
				output:
					result Report (1..1)
				
				set result:
					Report {
						subreportList:
							input -> values
								extract v [
									input
										extract titles
										then extract item + v to-string
										then extract Subsubreport { subsubbasic: item }
										then Subreport { subsubreportList: item }
								]
					}
			
			type ReportableInput:
				titles string (0..*)
				values int (0..*)
			
			type Report:
				subreportList Subreport (0..*)
			
			type Subreport:
				subsubreportList Subsubreport (0..*)
			
			type Subsubreport:
				subsubbasic string (1..1)
		'''
		val code = model.generateCode

		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.projections"), "ReportProjectionTypeTabulator", Tabulator)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val subsubreport1 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "1"})
		val subsubreport2 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "2"})
		val subsubreport3 = classes.createInstanceUsingBuilder("Subsubreport", #{"subsubbasic" -> "3"})
		val subreport1 = classes.createInstanceUsingBuilder("Subreport", #{"subsubreportList" -> #[subsubreport1, subsubreport2]})
		val subreport2 = classes.createInstanceUsingBuilder("Subreport", #{})
		val subreport3 = classes.createInstanceUsingBuilder("Subreport", #{"subsubreportList" -> #[subsubreport3]})
		val report = classes.createInstanceUsingBuilder("Report", #{"subreportList" -> #[subreport1, subreport2, subreport3]})
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		subreportList:
			subsubreportList:
				subsubbasic: 1
				subsubbasic: 2
			subsubreportList: <empty>
			subsubreportList:
				subsubbasic: 3
		'''
		assertFieldValuesEqual(expectedValues, flatReport)
	}
	
	@Test
	def void confirmToString() {
		val model = '''
			«HEADER»
			
			type ReportableInput:
				title string (1..1)
			
			type Report:
				basic string (1..1)
					[ruleReference Basic]
			
			
			reporting rule Basic from ReportableInput:
				extract title
		'''
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val tabulatorClass = reportId.toJavaReportTabulator
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		val report = classes.createInstanceUsingBuilder("Report", #{"basic" -> "My reportable input"})
		val actual = tabulator.tabulate(report)
		
		val expected = "[<basic, My reportable input>]"
		
		assertEquals(actual.toString(), expected)
	}
	
	final RootPackage MODEL3_PKG = new RootPackage("model3")
	final RootPackage MODEL3_META_PKG = new RootPackage("model3.metafields")
	final RootPackage META_PKG = new RootPackage("com.rosetta.model.metafields")
	
	def getModel3SingleCardinalityClasses() {
		val model = '''
			namespace model3
			
			type Root:
			    r1 string (1..1)
			    foo Foo (1..1)
			    bar Bar (1..1)
			
			type Foo:
			    f1 string (1..1)
			    barReference Bar (1..1)
			        [metadata reference]

			type Bar:
			    [metadata key]
			    b1 string (1..1)
	'''
		val code = model.generateCodeForModel(Model3RosettaConfigProvider)
		return #[code].compileToClassesForModel(Model3RosettaConfigProvider)
	}
	
	def getModel3RootTabulator(Map<String, Class<?>> classes) {
		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("model3.tabulator"), "RootTypeTabulator", Tabulator)
		return classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
	}
	
	@Test
	def void shouldTabulateSingleCardinalityTypes_ResolvedReference() {
		val classes = getModel3SingleCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator

		// bar and barReference is set
		val bar = classes.createInstanceUsingBuilder(MODEL3_PKG, "Bar", #{"b1" -> "b1Value"})
		val barReference = classes.createInstanceUsingBuilder(MODEL3_META_PKG, "ReferenceWithMetaBar", #{"value" -> bar})
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "f1Value", "barReference" -> barReference})
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "r1Value", "foo" -> foo, "bar" -> bar})
		assertEquals("Root {" + 
			"r1=r1Value, "+
			"foo=Foo {f1=f1Value, barReference=ReferenceWithMetaBar {value=Bar {b1=b1Value, meta=null}, globalReference=null, externalReference=null, reference=null}}, "+
			"bar=Bar {b1=b1Value, meta=null}}", 
			root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, r1Value>, <foo, {<f1, f1Value>, <barReference, {<b1, b1Value>}>}>, <bar, {<b1, b1Value>}>]", tabulatedRoot.toString)
	}
	
	@Test
	def void shouldTabulateSingleCardinalityTypes_EmptyReferenceAndValue() {
		val classes = getModel3SingleCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator
		
		 // bar and barReference is null
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "fValue"}) // bar not set
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "rValue", "foo" -> foo})
		assertEquals("Root {r1=rValue, foo=Foo {f1=fValue, barReference=null}, bar=null}", root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, rValue>, <foo, {<f1, fValue>, <barReference, <empty>>}>, <bar, <empty>>]", tabulatedRoot.toString)
	}

	@Test
	def void shouldTabulateSingleCardinalityTypes_UnresolvedReference() {
		val classes = getModel3SingleCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator

        // bar is set and barReference is set as an unresolved reference
		val barMetaFields = MetaFields.builder.setExternalKey("barExtKey").setGlobalKey("barGlobalKey")
		val bar = classes.createInstanceUsingBuilder(MODEL3_PKG, "Bar", #{"meta" -> barMetaFields, "b1" -> "bValue"})
		val barReference = classes.createInstanceUsingBuilder(MODEL3_META_PKG, "ReferenceWithMetaBar", #{"externalReference" -> "barExtKey", "globalReference" -> "barGlobalKey"})
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "fValue", "barReference" -> barReference})
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "rValue", "foo" -> foo, "bar" -> bar})
		assertEquals("Root {r1=rValue, foo=Foo {f1=fValue, barReference=ReferenceWithMetaBar {value=null, globalReference=barGlobalKey, externalReference=barExtKey, reference=null}}, bar=Bar {b1=bValue, meta=MetaFields {scheme=null, template=null, location=null, address=null, globalKey=barGlobalKey, externalKey=barExtKey, key=null}}}", 
			root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, rValue>, <foo, {<f1, fValue>, <barReference, <empty>>}>, <bar, {<b1, bValue>}>]", tabulatedRoot.toString)
	}

	def getModel3MulitCardinalityClasses() {
		val model = '''
			namespace model3
			
			type Root:
			    r1 string (1..1)
			    foo Foo (1..1)
			    bar Bar (1..1)
			
			type Foo:
			    f1 string (1..1)
			    barReferences Bar (1..*)
			        [metadata reference]

			type Bar:
			    [metadata key]
			    b1 string (1..1)
	'''
		val code = model.generateCodeForModel(Model3RosettaConfigProvider)
		#[code].compileToClassesForModel(Model3RosettaConfigProvider)
	}
	
	@Test
	def void shouldTabulateMultiCardinalityTypes_ResolvedReference() {
		val classes = getModel3MulitCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator

		// bar and barReference is set
		val bar = classes.createInstanceUsingBuilder(MODEL3_PKG, "Bar", #{"b1" -> "b1Value"})
		val barReference = classes.createInstanceUsingBuilder(MODEL3_META_PKG, "ReferenceWithMetaBar", #{"value" -> bar})
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "f1Value", "barReferences" -> #[barReference]})
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "r1Value", "foo" -> foo, "bar" -> bar})
		assertEquals("Root {r1=r1Value, " +
			"foo=Foo {f1=f1Value, barReferences=[ReferenceWithMetaBar {value=Bar {b1=b1Value, meta=null}, globalReference=null, externalReference=null, reference=null}]}, " +
			"bar=Bar {b1=b1Value, meta=null}}", 
			root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, r1Value>, <foo, {<f1, f1Value>, <barReferences, [{<b1, b1Value>}]>}>, <bar, {<b1, b1Value>}>]", tabulatedRoot.toString)
	}
	
	@Test
	def void shouldTabulateMultiCardinalityTypes_EmptyReferenceAndValue() {
		val classes = getModel3MulitCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator
		
		 // bar and barReference is null
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "fValue"}) // bar not set
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "rValue", "foo" -> foo})
		assertEquals("Root {r1=rValue, foo=Foo {f1=fValue, barReferences=null}, bar=null}", root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, rValue>, <foo, {<f1, fValue>, <barReferences, <empty>>}>, <bar, <empty>>]", tabulatedRoot.toString)
	}

	@Test
	def void shouldTabulateMultiCardinalityTypes_UnresolvedReference() {
		val classes = getModel3MulitCardinalityClasses
		val rootTabulator = classes.getModel3RootTabulator

        // bar is set and barReference is set as an unresolved reference
		val barMetaFields = MetaFields.builder.setExternalKey("barExtKey").setGlobalKey("barGlobalKey")
		val bar = classes.createInstanceUsingBuilder(MODEL3_PKG, "Bar", #{"meta" -> barMetaFields, "b1" -> "bValue"})
		val barReference = classes.createInstanceUsingBuilder(MODEL3_META_PKG, "ReferenceWithMetaBar", #{"externalReference" -> "barExtKey", "globalReference" -> "barGlobalKey"})
		val foo = classes.createInstanceUsingBuilder(MODEL3_PKG, "Foo", #{"f1" -> "fValue", "barReferences" -> #[barReference]})
		val root = classes.createInstanceUsingBuilder(MODEL3_PKG, "Root", #{"r1" -> "rValue", "foo" -> foo, "bar" -> bar})
		assertEquals("Root {r1=rValue, foo=Foo {f1=fValue, barReferences=[ReferenceWithMetaBar {value=null, globalReference=barGlobalKey, externalReference=barExtKey, reference=null}]}, bar=Bar {b1=bValue, meta=MetaFields {scheme=null, template=null, location=null, address=null, globalKey=barGlobalKey, externalKey=barExtKey, key=null}}}",
			root.toString)
		
		val tabulatedRoot = rootTabulator.tabulate(root)	
		
		assertNotNull(tabulatedRoot)
		assertEquals("[<r1, rValue>, <foo, {<f1, fValue>, <barReferences, []>}>, <bar, {<b1, bValue>}>]", tabulatedRoot.toString)
	}
	
	private static class Model3RosettaConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-tabulator-type-config-model3.yml")
		}
	}
}
