package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ExternalHashcodeGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper

	@Test
	def void shouldGenerateExternalHashMethod() {
		val code = '''
			enum Enum: one

			type RosettaType:

			type PlainOldRosettaObject:
				basicTypE string (1..1)
				basicTypeList string (1..*)
				rosettaObject RosettaType (1..1)
				rosettaObjectList RosettaType (1..*)
				enumeration Enum (1..1)
				enumerationList Enum (1..*)
		'''.generateCode
		//code.writeClasses("shouldGenerateExternalHashMethod")		
		val classes = code.compileToClasses
		val poro = classes.get(rootPackage + '.PlainOldRosettaObject')

		assertThat(poro.declaredMethods.map[name], hasItem('process'))
	}
	
	@Test
	def void shouldHandleSuperClass() {
		'''
			type Super:
			type Sub extends Super:
				basicTypE string (1..1)
		'''.generateCode.compileToClasses
	}
	
	@Test
	def void shouldHandleEmptyClass() {
		'''
			type Empty:
		'''.generateCode.compileToClasses
	}
	
	@Test
	def void shouldHandleGlobalKeys() {
		val code = '''
			type WithGlobalKey:
				[metadata key]
				foo string (1..1)
		'''.generateCode
		//code.writeClasses("ShouldHandleGlobalKeys")		
		code.compileToClasses
	}
	
	@Test
	def void shouldNotGenerateForEnums() {
		val code = '''
			enum Enum: foo
		'''.generateCode
		
		code.compileToClasses
	}
}
