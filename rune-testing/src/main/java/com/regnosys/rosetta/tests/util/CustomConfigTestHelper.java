package com.regnosys.rosetta.tests.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

public class CustomConfigTestHelper {
	public static Map<String, Class<?>> compileToClassesForModel(List<Map<String, String>> code,
			Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		CodeGeneratorTestHelper codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider);
		Map<String, String> generatedCode = new HashMap<>();
		code.forEach(generatedCode::putAll);
		return codeGeneratorTestHelper.compileToClasses(generatedCode);
	}

	public static Map<String, String> generateCodeForModel(CharSequence model,
			Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		CodeGeneratorTestHelper codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider);
		return codeGeneratorTestHelper.generateCode(model);
	}

	public static Map<String, String> generateCodeForModel(List<? extends CharSequence> models,
			Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		CodeGeneratorTestHelper codeGeneratorTestHelper = getCodeGeneratorTestHelper(configurationFileProvider);
		return codeGeneratorTestHelper.generateCode(models.toArray(CharSequence[]::new));
	}

	private static CodeGeneratorTestHelper getCodeGeneratorTestHelper(
			Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		Injector injector = getInjector(configurationFileProvider);
		return injector.getInstance(CodeGeneratorTestHelper.class);
	}

	private static Injector getInjector(Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {
		RosettaCustomConfigInjectorProvider provider = createProvider(configurationFileProvider);
		return provider.getInjector();
	}

	private static RosettaCustomConfigInjectorProvider createProvider(
			Class<? extends RosettaConfigurationFileProvider> configurationFileProvider) {

		return new RosettaCustomConfigInjectorProvider() {

			@Override
			protected RosettaRuntimeModule createRuntimeModule() {

				return new RosettaRuntimeModule() {
					@Override
					public ClassLoader bindClassLoaderToInstance() {
						return RosettaTestInjectorProvider.class.getClassLoader();
					}

					public Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
						return configurationFileProvider;
					}
				};
			}
		};
	}
}
