package com.regnosys.rosetta.interpreternew

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaInterpreterCompilerComparisonTest2 {
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def simpleTest(){
		val code = '''
		2
		'''.generateCode
	}
}