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
class RosettaKeyGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper

	@Test
	def void shouldGenerateRosettaKeyFieldAndGetterWhenSet() {
		val code = '''
			class WithRosettaKey key rosettaKeyValue {
				foo string (1..1);
			}
		'''.generateCode

		val classess = code.compileToClasses
		val withRosettaKey = classess.get(rootPackage.name + '.WithRosettaKey')

		assertThat(withRosettaKey.declaredFields.map[name], hasItem('meta'))
		assertThat(withRosettaKey.declaredFields.map[name], hasItem('rosettaKeyValue'))
	}

	@Test
	def void shouldGenerateRosettaKeyValueFieldAndGetterWhenSet() {
		val code = '''
			class WithRosettaKey key {
				foo string (1..1);
			}
		'''.generateCode

		val classess = code.compileToClasses
		val withRosettaKey = classess.get(rootPackage.name + '.WithRosettaKey')

		assertThat(withRosettaKey.methods.exists[name.equals('getMeta')], is(true))
		assertThat(withRosettaKey.methods.exists[name.equals('getRosettaKeyValue')], is(false))
	}

	@Test
	def void shouldNotGenerateFieldsAndGetterWhenNotDefined() {
		val code = '''
			class WithoutRosettaKeys {
				foo string (1..1);
			}
		'''.generateCode

		val classess = code.compileToClasses
		val withoutRosettaKeys = classess.get(rootPackage.name + '.WithoutRosettaKeys')

		assertThat(withoutRosettaKeys.fields.map[name], not(hasItem('metaFields')))

		assertThat(withoutRosettaKeys.methods.exists[name.equals('meta')], is(false))
	}
	
	@Test
	def void shouldGenerateGetterWhenRosettaKeyValueDefined() {
		val code = '''
			class WithRosettaKeyValue rosettaKeyValue {
				foo string (1..1);
			}
		'''.generateCode

		val classess = code.compileToClasses
		val withRosettaKeyValue = classess.get(rootPackage.name + '.WithRosettaKeyValue')

		assertThat(withRosettaKeyValue.methods.exists[name.equals('getRosettaKey')], is(false))
		assertThat(withRosettaKeyValue.methods.exists[name.equals('getRosettaKeyValue')], is(true))
	}

	@Test
	def void shouldGenerateGettersWhenRosettaKeyAndRosettaKeyValueDefined() {
		val code = '''
			class WithRosettaKeys key rosettaKeyValue {
				foo string (1..1);
			}
		'''.generateCode

		val classess = code.compileToClasses
		val withRosettaKeys = classess.get(rootPackage.name + '.WithRosettaKeys')

		assertThat(withRosettaKeys.methods.exists[name.equals('getMeta')], is(true))
		assertThat(withRosettaKeys.methods.exists[name.equals('getRosettaKeyValue')], is(true))
	}
}
