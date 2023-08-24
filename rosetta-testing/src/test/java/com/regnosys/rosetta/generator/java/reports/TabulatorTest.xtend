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
				result.newLine
				for (v: fieldValue.value.get) {
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
}