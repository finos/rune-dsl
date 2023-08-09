package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.util.DottedPath
import com.rosetta.util.types.JavaClass
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class RuleGeneratorTest {
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void generateSingleReportingRule() {
		val model = '''
			namespace com.rosetta.test.model
			
			type ReportableInput:
				title string (1..1)
			

			reporting rule Basic from ReportableInput:
				extract title
		'''
		
		val code = model.generateCode
		
		val basicRuleClass = new JavaClass(DottedPath.splitOnDots("com.rosetta.test.model.reports"), "BasicRule")
		
		val basicruleCode = code.get(basicRuleClass.canonicalName.withDots)
		
		val expected = '''
			public class BasicRule implements ReportFunction<ReportableInput, String> {
				@Override
				protected String doEvaluate(ReportableInput input) {
					
			}
		'''
		
		assertEquals(expected, basicruleCode)
		
	}

	
}