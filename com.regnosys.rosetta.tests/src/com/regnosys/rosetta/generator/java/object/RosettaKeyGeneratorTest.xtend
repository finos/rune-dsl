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
class GlobalKeyGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper

	@Test
	def void shouldGenerateGlobalKeyFieldAndGetterWhenSet() {
		val code = '''
			type WithGlobalKey:
				[metadata key]
				foo string (1..1)
		'''.generateCode

		val classess = code.compileToClasses
		val withGlobalKey = classess.get(rootPackage.name + '.WithGlobalKey')

		assertThat(withGlobalKey.declaredFields.map[name], hasItem('meta'))
	}

	@Test
	def void shouldNotGenerateFieldsAndGetterWhenNotDefined() {
		val code = '''
			type WithoutGlobalKeys:
				foo string (1..1)
		'''.generateCode

		val classess = code.compileToClasses
		val withoutGlobalKeys = classess.get(rootPackage.name + '.WithoutGlobalKeys')

		assertThat(withoutGlobalKeys.fields.map[name], not(hasItem('metaFields')))

		assertThat(withoutGlobalKeys.methods.exists[name.equals('meta')], is(false))
	}
}
