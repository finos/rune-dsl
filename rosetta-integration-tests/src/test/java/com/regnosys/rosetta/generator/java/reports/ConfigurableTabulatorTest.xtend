package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.RosettaCustomConfigInjectorProvider
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.util.DottedPath
import com.rosetta.util.types.generated.GeneratedJavaClass
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.assertNotNull

@InjectWith(RosettaCustomConfigInjectorProvider)
@ExtendWith(InjectionExtension)
class ConfigurableTabulatorTest {
		
	@Inject extension CodeGeneratorTestHelper
	
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
	def void generateNestedTabulatorFromAnnotationPresentInConfig() {
		val model = '''
			«HEADER»
			
			func MyFunc:
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

		val tabulatorClass = new GeneratedJavaClass(DottedPath.splitOnDots("com.rosetta.test.model.tabulator"), "ReportMyFuncTypeTabulator", Tabulator)
		
		val tabulatorContainerClass = code.get("com.rosetta.test.model.tabulator.MyFuncTabulator")
		
		assertNotNull(tabulatorContainerClass)
		
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
}