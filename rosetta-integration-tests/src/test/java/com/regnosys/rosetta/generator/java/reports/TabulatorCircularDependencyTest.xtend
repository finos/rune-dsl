package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.RosettaCustomConfigInjectorProvider
import jakarta.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.Assert.assertNotNull

@InjectWith(RosettaCustomConfigInjectorProvider)
@ExtendWith(InjectionExtension)
class TabulatorCircularDependencyTest {
	
	@Inject extension CodeGeneratorTestHelper

	@Test
	def void generateTabulatorWithCircularDependency() {
		val model = '''
			namespace com.rosetta.test.model.circular
			
			annotation projection:
			
			type Foo:
				foo Foo (1..1)
				
			func CreateFoo:
				[projection]
				output:
					foo Foo (1..1)
			
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		assertNotNull(classes)
	}
}
