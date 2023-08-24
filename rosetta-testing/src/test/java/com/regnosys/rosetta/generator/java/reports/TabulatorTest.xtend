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
import java.util.Map
import com.rosetta.util.DottedPath
import com.rosetta.util.types.GeneratedJavaClassService
import com.rosetta.util.types.JavaClass
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.model.lib.RosettaModelObject
import java.util.List
import com.rosetta.model.lib.reports.Tabulator.Field
import org.eclipse.xtend2.lib.StringConcatenationClient
import java.math.BigDecimal
import com.google.inject.Injector
import com.rosetta.model.lib.reports.Tabulator.FieldValue
import com.rosetta.model.lib.reports.Tabulator.FieldValueVisitor
import com.rosetta.model.lib.reports.Tabulator.MultiNestedFieldValue
import com.rosetta.model.lib.reports.Tabulator.NestedFieldValue
import org.eclipse.xtend2.lib.StringConcatenation
import com.rosetta.model.lib.ModelSymbolId
import javax.inject.Inject
import com.rosetta.model.lib.records.Date
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import java.time.LocalTime
import java.time.ZonedDateTime

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class TabulatorTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension GeneratedJavaClassService
	@Inject Injector injector
	
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
	'''
	
	private def Tabulator<RosettaModelObject> createTabulatorInstance(Map<String, Class<?>> classes, JavaClass tabulatorClassRepr) {
		val tabulatorClass = classes.get(tabulatorClassRepr.canonicalName.withDots)
		return injector.getInstance(tabulatorClass) as Tabulator<RosettaModelObject>
	}
	
	private def void assertFieldsEqual(String expected, List<Field> actual) {
		val actualRepr =
		'''
		«FOR field : actual SEPARATOR '\n'»
			«field.fieldStringRepr»
		«ENDFOR»
		'''
		assertEquals(expected, actualRepr)
	}
	private def StringConcatenationClient fieldStringRepr(Field field) {
		'''
		«field.attributeName»«IF field.multi»*«ENDIF»
		«IF field.name != field.attributeName»"«field.name»"«ENDIF»
			«FOR child : field.children SEPARATOR '\n'»
			«child.fieldStringRepr»
			«ENDFOR»
		'''
	}
	private def void assertFieldValuesEqual(String expected, List<FieldValue> actual) {
		val actualRepr =
		'''
		«FOR fieldValue : actual SEPARATOR '\n'»
			«fieldValue.fieldValueStringRepr»
		«ENDFOR»
		'''
		assertEquals(expected, actualRepr)
	}
	
	private static def StringConcatenationClient fieldValueStringRepr(FieldValue fieldValue) {
		val visitor = new FieldValueToReprVisitor
		fieldValue.accept(visitor, null)
		'''«visitor.result»'''
	}
	private static class FieldValueToReprVisitor implements FieldValueVisitor<Void> {
		public StringConcatenation result = new StringConcatenation
		override visitMultiNested(MultiNestedFieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(':')
			if (fieldValue.isPresent) {
				for (vs: fieldValue.value.get) {
					for (v: vs) {
						result.newLine
						result.append("\t")
						result.append(v.fieldValueStringRepr, "\t")
					}
				}
			} else {
				result.append(' <empty>')
			}
		}
		override visitNested(NestedFieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(':')
			if (fieldValue.isPresent) {
				for (v: fieldValue.value.get) {
					result.newLine
					result.append("\t")
					result.append(v.fieldValueStringRepr, "\t")
				}
			} else {
				result.append(' <empty>')
			}
		}
		override visitSingle(FieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(': ')
			result.append(fieldValue.value.map[toString].orElse("<empty>"))
		}
	}
	
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
			
			func Create_Subreport:
				inputs: subbasic string (1..1)
				output: result Subreport (1..1)
				set result->subbasic: subbasic + " - created"
			
			reporting rule SubreportWithRule from ReportableInput:
				Create_Subreport(Subbasic)
					as "Subreport from a rule"
		'''
		val code = model.generateCode
		
		val reportId =  ModelSymbolId.fromRegulatoryReference(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val reportTabulatorClass = reportId.toJavaReportTabulator
		
		val reportTabulatorCode = code.get(reportTabulatorClass.canonicalName.withDots)
		assertThat(reportTabulatorCode, CoreMatchers.notNullValue())
		var expected = '''
			package com.rosetta.test.model.reports;
			
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.test.model.Report;
			import java.util.List;
			import javax.inject.Inject;
			
			
			public class TEST_REGCorpReportTabulator implements Tabulator<Report> {
				private ReportTabulator tabulator;
				
				@Inject
				public TEST_REGCorpReportTabulator(ReportTabulator tabulator) {
					this.tabulator = tabulator;
				}
				
				@Override
				public List<Field> getFields() {
					return tabulator.getFields();
				}
				
				@Override
				public List<FieldValue> tabulate(Report report) {
					return tabulator.tabulate(report);
				}
			}
		'''
		assertEquals(expected, reportTabulatorCode)
		
		val tabulatorClass = new JavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTabulator")
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		expected = '''
			package com.rosetta.test.model.reports;
			
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
			
			
			public class ReportTabulator implements Tabulator<Report> {
				private final Field basicField;
				private final Field subreportField;
				private final Field subreportWithRuleField;
				
				private final SubreportTabulator subreportTabulator;
				
				@Inject
				public ReportTabulator(SubreportTabulator subreportTabulator) {
					this.subreportTabulator = subreportTabulator;
					this.basicField = new FieldImpl(
						"basic",
						false,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "Basic")),
						Optional.empty(),
						Arrays.asList()
					);
					this.subreportField = new FieldImpl(
						"subreport",
						false,
						Optional.empty(),
						Optional.empty(),
						subreportTabulator.getFields()
					);
					this.subreportWithRuleField = new FieldImpl(
						"subreportWithRule",
						false,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "SubreportWithRule")),
						Optional.of("Subreport from a rule"),
						subreportTabulator.getFields()
					);
				}
				
				@Override
				public List<Field> getFields() {
					return Arrays.asList(basicField, subreportField, subreportWithRuleField);
				}
				
				@Override
				public List<FieldValue> tabulate(Report report) {
					Optional<String> basic = Optional.ofNullable(report.getBasic());
					Optional<List<FieldValue>> subreport = Optional.ofNullable(report.getSubreport())
						.map(x -> subreportTabulator.tabulate(x));
					Optional<List<FieldValue>> subreportWithRule = Optional.ofNullable(report.getSubreportWithRule())
						.map(x -> subreportTabulator.tabulate(x));
					return Arrays.asList(
						new FieldValueImpl(basicField, basic),
						new NestedFieldValueImpl(subreportField, subreport),
						new NestedFieldValueImpl(subreportWithRuleField, subreportWithRule)
					);
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
			
			func Create_Subreport:
				inputs:
					subbasic string (1..1)
					n int (0..1)
				output: result Subreport (1..1)
				set result->subbasic: subbasic + " - created"
				set result->n: n
			
			reporting rule SubreportList from ReportableInput:
				extract reportableInput [
					reportableInput
						extract values
						then extract Subbasic(reportableInput) + item to-string
						then extract Create_Subreport(item, N(reportableInput))
				]
					as "Subreport group"
		'''
		val code = model.generateCode

		val tabulatorClass = new JavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTabulator")
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.reports;
			
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
			
			
			public class ReportTabulator implements Tabulator<Report> {
				private final Field basicListField;
				private final Field subreportListField;
				
				private final SubreportTabulator subreportTabulator;
				
				@Inject
				public ReportTabulator(SubreportTabulator subreportTabulator) {
					this.subreportTabulator = subreportTabulator;
					this.basicListField = new FieldImpl(
						"basicList",
						true,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "BasicList")),
						Optional.empty(),
						Arrays.asList()
					);
					this.subreportListField = new FieldImpl(
						"subreportList",
						true,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "SubreportList")),
						Optional.of("Subreport group"),
						subreportTabulator.getFields()
					);
				}
				
				@Override
				public List<Field> getFields() {
					return Arrays.asList(basicListField, subreportListField);
				}
				
				@Override
				public List<FieldValue> tabulate(Report report) {
					Optional<List<? extends Integer>> basicList = Optional.ofNullable(report.getBasicList());
					Optional<List<List<FieldValue>>> subreportList = Optional.ofNullable(report.getSubreportList())
						.map(x -> x.stream()
							.map(_x -> subreportTabulator.tabulate(_x))
							.collect(Collectors.toList()));
					return Arrays.asList(
						new FieldValueImpl(basicListField, basicList),
						new MultiNestedFieldValueImpl(subreportListField, subreportList)
					);
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
			
			func Create_Subreport:
				inputs: subsubreportList Subsubreport (0..*)
				output: result Subreport (1..1)
				set result->subsubreportList: subsubreportList
			
			func Create_Subsubreport:
				inputs: subsubbasic string (1..1)
				output: result Subsubreport (1..1)
				set result->subsubbasic: subsubbasic
			
			reporting rule SubsubreportList from string:
				extract Create_Subsubreport
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
								then Create_Subreport
						]
				]
					as "Subreport group"
		'''
		val code = model.generateCode

		val tabulatorClass = new JavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTabulator")
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
		
		val tabulatorClass = new JavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "ReportTabulator")
		
		val tabulatorCode = code.get(tabulatorClass.canonicalName.withDots)
		assertThat(tabulatorCode, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.reports;
			
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
			
			
			public class ReportTabulator implements Tabulator<Report> {
				private final Field basic1Field;
				private final Field basic2Field;
				private final Field basic3Field;
				
				public ReportTabulator() {
					this.basic1Field = new FieldImpl(
						"basic1",
						false,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "Basic1")),
						Optional.empty(),
						Arrays.asList()
					);
					this.basic2Field = new FieldImpl(
						"basic2",
						false,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "Basic2Modified")),
						Optional.of("Basic 2 Rule - Modified"),
						Arrays.asList()
					);
					this.basic3Field = new FieldImpl(
						"basic3",
						false,
						Optional.of(DottedPath.of("com", "rosetta", "test", "model", "Basic3")),
						Optional.empty(),
						Arrays.asList()
					);
				}
				
				@Override
				public List<Field> getFields() {
					return Arrays.asList(basic1Field, basic2Field, basic3Field);
				}
				
				@Override
				public List<FieldValue> tabulate(Report report) {
					Optional<String> basic1 = Optional.ofNullable(report.getBasic1());
					Optional<Integer> basic2 = Optional.ofNullable(report.getBasic2());
					Optional<BigDecimal> basic3 = Optional.ofNullable(report.getBasic3());
					return Arrays.asList(
						new FieldValueImpl(basic1Field, basic1),
						new FieldValueImpl(basic2Field, basic2),
						new FieldValueImpl(basic3Field, basic3)
					);
				}
			}
		'''
		assertEquals(expected, tabulatorCode)
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
		
		val reportId = ModelSymbolId.fromRegulatoryReference(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
		
		val reportId = ModelSymbolId.fromRegulatoryReference(DottedPath.splitOnDots("com.rosetta.test.model"), "TEST_REG", "Corp")
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
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
			    power PowerEnum (0..1) <"Multiple cardinality not supported">
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
			    [legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "9"  provision "."]
			    extract repeatable Person -> organisations then
			    (
			        OrganisationName,
			        OrganisationCountry,
			        IsGovernmentAgency
			    ) as "Hero Organisations"
			
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
			
			// Validation catches this if added to report with wrong type
			reporting rule MultipleTypes from Person: <"Multiple Types - ZonedDateTime and Number">
			    [legacy-syntax]
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "14"  provision "."]
			    (
			        extract Person -> attribute -> heroZonedDateTime as "Multiple Types - ZonedDateTime",
			        extract Person -> attribute -> heroNumber as "Multiple Types - Number"
			    )
		'''
		val code = model.generateCode
		
		val reportId = ModelSymbolId.fromRegulatoryReference(DottedPath.splitOnDots("test.reg"), "Shield", "Avengers", "SokoviaAccords")
		val tabulatorClass = reportId.toJavaReportTabulator
		
		val classes = code.compileToClasses
		val tabulator = classes.createTabulatorInstance(tabulatorClass)
		
		val expectedFields =
		'''
		heroName
		"Hero Name"
		
		dateOfBirth
		"Date of Birth"
		
		nationality
		"Nationality"
		
		hasSpecialAbilities
		"Has Special Abilities"
		
		power
		"Powers"
		
		attribute
			heroInt
			"Attribute - Int"
			
			heroNumber
			"Attribute - Number"
			
			heroTime
			"Attribute - Time"
			
			heroZonedDateTime
			"Attribute - ZonedDateTime"
		
		organisations*
		"Hero Organisations"
			name
			"Organisation Name"
			
			isGovernmentAgency
			"Is Government Agency"
			
			country
			"Organisation Country"
		
		notModelled
		"Not Modelled"
		'''
		assertFieldsEqual(expectedFields, tabulator.fields)
		
		val countryLoader = classes
			.get("test.reg.CountryEnum")
			.getDeclaredMethod("fromDisplayName", String)
		val powerLoader = classes
			.get("test.reg.PowerEnum")
			.getDeclaredMethod("fromDisplayName", String)
		
		val attributeReport = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "AttributeReport",
			#{
				"heroInt" -> 9,
				"heroNumber" -> new BigDecimal("0.12345"),
				"heroTime" -> LocalTime.parse("10:13:07"),
				"heroZonedDateTime" -> ZonedDateTime.parse("2011-06-01T10:13:07Z")
			}
		)
		val organisationReport1 = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "OrganisationReport",
			#{
				"name" -> "Avengers",
				"isGovernmentAgency" -> false,
				"country" -> countryLoader.invoke(null, "UnitedStatesOfAmerica")
			}
		)
		val organisationReport2 = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "OrganisationReport",
			#{
				"name" -> "S.H.I.E.L.D.",
				"isGovernmentAgency" -> true,
				"country" -> countryLoader.invoke(null, "UnitedStatesOfAmerica")
			}
		)
		val report = classes.createInstanceUsingBuilder(new RootPackage("test.reg"), "SokoviaAccordsReport",
			#{
				"heroName" -> "Iron Man",
				"dateOfBirth" -> Date.of(1970, 5, 29),
				"nationality" -> countryLoader.invoke(null, "UnitedStatesOfAmerica"),
				"hasSpecialAbilities" -> true,
				"power" -> powerLoader.invoke(null, "SuperhumanStrength"),
				"attribute" -> attributeReport,
				"organisations" -> #[organisationReport1, organisationReport2],
				"notModelled" -> "Not modelled"
			}
		)
		val flatReport = tabulator.tabulate(report)
		val expectedValues =
		'''
		heroName: Iron Man
		
		dateOfBirth: 1970-05-29
		
		nationality: UnitedStatesOfAmerica
		
		hasSpecialAbilities: true
		
		power: SuperhumanStrength
		
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
}