package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.junit.jupiter.api.Test
import com.regnosys.rosetta.config.RosettaCustomConfigInjectorProvider
import com.regnosys.rosetta.RosettaRuntimeModule
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider
import java.net.URL
import com.google.inject.Injector
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import java.util.List
import java.util.HashMap

@InjectWith(RosettaInjectorProvider)
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

	def compileToClassesForModel(List<HashMap<String, String>> code,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		val generatedCode = newHashMap
		code.forEach[it.forEach[k, v|generatedCode.put(k, v)]]
		codeGeneratorTestHelper.compileToClasses(generatedCode)
	}

	def generateCodeForModel(CharSequence model,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		codeGeneratorTestHelper.generateCode(model)
	}

	def generateCodeForModel(List<? extends CharSequence> models,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		codeGeneratorTestHelper.generateCode(models)
	}

	def CodeGeneratorTestHelper getCodeGeneratorTestHelper(
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val injector = getInjector(configurationFileProvider)
		injector.getInstance(CodeGeneratorTestHelper)
	}

	def Injector getInjector(Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val provider = createProvider(configurationFileProvider)
		provider.injector
	}

	def RosettaCustomConfigInjectorProvider createProvider(
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {

		new RosettaCustomConfigInjectorProvider() {

			override RosettaRuntimeModule createRuntimeModule() {

				new RosettaRuntimeModule() {
					override ClassLoader bindClassLoaderToInstance() {
						RosettaInjectorProvider.getClassLoader()
					}

					def Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
						return configurationFileProvider
					}
				}
			}
		}
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
