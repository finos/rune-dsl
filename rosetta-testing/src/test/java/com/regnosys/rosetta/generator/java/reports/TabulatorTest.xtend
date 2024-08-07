package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import com.rosetta.util.DottedPath
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.model.lib.RosettaModelObject
import org.eclipse.xtend2.lib.StringConcatenationClient
import java.math.BigDecimal
import javax.inject.Inject
import com.rosetta.model.lib.ModelReportId
import com.rosetta.util.types.generated.GeneratedJavaClassService
import com.rosetta.util.types.generated.GeneratedJavaClass

@InjectWith(RosettaInjectorProvider)
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
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.test.model.Report;
			import java.util.List;
			import javax.inject.Inject;

			
			@ImplementedBy(TEST_REGCorpReportTabulator.Impl.class)
			public interface TEST_REGCorpReportTabulator extends Tabulator<Report> {
				class Impl implements TEST_REGCorpReportTabulator {
					private final ReportTypeTabulator tabulator;
					
					@Inject
					public Impl(ReportTypeTabulator tabulator) {
						this.tabulator = tabulator;
					}
					
					@Override
					public List<Field> getFields() {
						return tabulator.getFields();
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

			
			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
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
							subreportTypeTabulator.getFields()
						);
						this.subreportWithRuleField = new FieldImpl(
							"subreportWithRule",
							false,
							Optional.of(new ModelSymbolId(DottedPath.of("com", "rosetta", "test", "model"), "SubreportWithRule")),
							Optional.of("Subreport from a rule"),
							subreportTypeTabulator.getFields()
						);
					}
					
					@Override
					public List<Field> getFields() {
						return Arrays.asList(basicField, subreportField, subreportWithRuleField);
					}
					
					@Override
					public List<FieldValue> tabulate(Report input) {
						Optional<String> basic = Optional.ofNullable(input.getBasic());
						Optional<List<FieldValue>> subreport = Optional.ofNullable(input.getSubreport())
							.map(x -> subreportTypeTabulator.tabulate(x));
						Optional<List<FieldValue>> subreportWithRule = Optional.ofNullable(input.getSubreportWithRule())
							.map(x -> subreportTypeTabulator.tabulate(x));
						return Arrays.asList(
							new FieldValueImpl(basicField, basic),
							new NestedFieldValueImpl(subreportField, subreport),
							new NestedFieldValueImpl(subreportWithRuleField, subreportWithRule)
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val expectedFields =
		'''
		basic
		
		subreport
			subbasic
		
		subreportWithRule
		"Subreport from a rule"
			subbasic
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
			

			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
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
							subreportTypeTabulator.getFields()
						);
					}
					
					@Override
					public List<Field> getFields() {
						return Arrays.asList(basicListField, subreportListField);
					}
					
					@Override
					public List<FieldValue> tabulate(Report input) {
						Optional<List<? extends Integer>> basicList = Optional.ofNullable(input.getBasicList());
						Optional<List<List<FieldValue>>> subreportList = Optional.ofNullable(input.getSubreportList())
							.map(x -> x.stream()
								.map(_x -> subreportTypeTabulator.tabulate(_x))
								.collect(Collectors.toList()));
						return Arrays.asList(
							new FieldValueImpl(basicListField, basicList),
							new MultiNestedFieldValueImpl(subreportListField, subreportList)
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val expectedFields =
		'''
		basicList*
		
		subreportList*
		"Subreport group"
			subbasic
			"Basic value of subreport"
			
			n
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
		
		val expectedFields =
		'''
		subreportList*
		"Subreport group"
			subsubreportList*
			"List of subsubreports"
				subsubbasic
				"Basic value of subsubreport"
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
			import java.math.BigDecimal;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;

			
			@ImplementedBy(ReportTypeTabulator.Impl.class)
			public interface ReportTypeTabulator extends Tabulator<Report> {
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
					public List<Field> getFields() {
						return Arrays.asList(basic1Field, basic2Field, basic3Field);
					}
					
					@Override
					public List<FieldValue> tabulate(Report input) {
						Optional<String> basic1 = Optional.ofNullable(input.getBasic1());
						Optional<Integer> basic2 = Optional.ofNullable(input.getBasic2());
						Optional<BigDecimal> basic3 = Optional.ofNullable(input.getBasic3());
						return Arrays.asList(
							new FieldValueImpl(basic1Field, basic1),
							new FieldValueImpl(basic2Field, basic2),
							new FieldValueImpl(basic3Field, basic3)
						);
					}
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.<Tabulator<RosettaModelObject>>createInstance(tabulatorClass)
		
		val expectedFields =
		'''
		basic1
		
		basic2
		"Basic 2 Rule - Modified"
		
		basic3
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
		
		val expectedFields =
		'''
		basic1
		"Inline"
		
		basic2
		"Basic 2 Rule modified"
		
		basic3
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
		
		val expectedFields =
		'''
		subreportList*
			subsubreportList*
				subsubbasic
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
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
}