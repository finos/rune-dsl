package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider
import java.util.List
import com.google.inject.Injector
import com.regnosys.rosetta.RosettaRuntimeModule
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import java.util.Map

class CustomConfigTestHelper {
	static def compileToClassesForModel(List<Map<String, String>> code,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		val generatedCode = newHashMap
		code.forEach[it.forEach[k, v|generatedCode.put(k, v)]]
		codeGeneratorTestHelper.compileToClasses(generatedCode)
	}

	static def generateCodeForModel(CharSequence model,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		codeGeneratorTestHelper.generateCode(model)
	}

	static def generateCodeForModel(List<? extends CharSequence> models,
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider)
		codeGeneratorTestHelper.generateCode(models)
	}

	static private def CodeGeneratorTestHelper getCodeGeneratorTestHelper(
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val injector = getInjector(configurationFileProvider)
		injector.getInstance(CodeGeneratorTestHelper)
	}

	static private def Injector getInjector(Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		val provider = createProvider(configurationFileProvider)
		provider.getInjector
	}

	static private def RosettaCustomConfigInjectorProvider createProvider(
		Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {

		new RosettaCustomConfigInjectorProvider() {

			override RosettaRuntimeModule createRuntimeModule() {

				new RosettaRuntimeModule() {
					override ClassLoader bindClassLoaderToInstance() {
						RosettaTestInjectorProvider.getClassLoader()
					}

					def Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
						return configurationFileProvider
					}
				}
			}
		}
	}
}