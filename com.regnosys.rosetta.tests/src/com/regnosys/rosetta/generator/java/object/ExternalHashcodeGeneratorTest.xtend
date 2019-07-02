package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ExternalHashcodeGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper

	@Test
	def void shouldGenerateExternalHashMethod() {
		val code = '''
			enum Enum {
				one
			}
			
			class RosettaType {
			}
			
			class PlainOldRosettaObject {
				basicType string (1..1);
				basicTypeList string (1..*);
				rosettaObject RosettaType (1..1);
				rosettaObjectList RosettaType (1..*);
				enumeration Enum (1..1);
				enumerationList Enum (1..*);
				
			}
		'''.generateCode
		//code.writeClasses("shouldGenerateExternalHashMethod")		
		val classess = code.compileToClasses
		val poro = classess.get(javaPackages.model.packageName + '.PlainOldRosettaObject')

		assertThat(poro.declaredMethods.map[name], hasItem('process'))
	}
	
	@Test
	def void shouldHandleSuperClass() {
		'''
			class Super {
			}
			
			class Sub extends Super {
				basicType string (1..1);
			}
		'''.generateCode.compileToClasses
	}
	
	@Test
	def void shouldHandleEmptyClass() {
		'''
			class Empty {}
		'''.generateCode.compileToClasses
	}
	
	@Test
	def void shouldHandleRosettaKeys() {
		val code = '''
			class WithRosettaKey key {
				foo string (1..1);
			}
		'''.generateCode
		code.writeClasses("ShouldHandleRosettaKeys")		
		code.compileToClasses
	}
	
	@Test
	def void shouldNotGenerateForEnums() {
		val code = '''
			enum Enum {
				foo
			}
		'''.generateCode
		
		code.compileToClasses
	}
}
