package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider
import java.net.URL
import org.junit.jupiter.api.Test

import static extension com.regnosys.rosetta.tests.util.CustomConfigTestHelper.*

class ModelMetaGeneratorFilteredNamespaceTest {

	@Test
	def void shouldGenerateBasicTypeReferencesFoo() {
		val model1 = '''
			namespace model1
			
				type Foo:
		'''

		val model1Code = model1.generateCodeForModel(Model1FileConfigProvider)

		val model2 = '''
			namespace model2
			
			import model1.Foo
			
				type Bar:
					foo Foo (1..1)
					[metadata reference]
		'''

		val model2Code = #[model1, model2].generateCodeForModel(Model2FileConfigProvider)

		#[model1Code, model2Code].compileToClassesForModel(Model2FileConfigProvider)
	}

	private static class Model1FileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-filtered-config-model1.yml")
		}
	}

	private static class Model2FileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-filtered-config-model2.yml")
		}
	}
}
