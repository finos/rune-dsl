package com.regnosys.rosetta.generator.java.object;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider;
import com.regnosys.rosetta.tests.util.CustomConfigTestHelper;

class ModelMetaGeneratorFilteredNamespaceTest {

	@Test
	void shouldGenerateBasicTypeReferencesFoo() {
		String model1 = """
				namespace model1

					type Foo:
				""";

		Map<String, String> model1Code =
				CustomConfigTestHelper.generateCodeForModel(model1, Model1FileConfigProvider.class);

		String model2 = """
				namespace model2

				import model1.Foo

					type Bar:
						foo Foo (1..1)
						[metadata reference]
				""";

		Map<String, String> model2Code =
				CustomConfigTestHelper.generateCodeForModel(List.of(model1, model2), Model2FileConfigProvider.class);

		CustomConfigTestHelper.compileToClassesForModel(List.of(model1Code, model2Code), Model2FileConfigProvider.class);
	}

	private static class Model1FileConfigProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-filtered-config-model1.yml");
		}
	}

	private static class Model2FileConfigProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-filtered-config-model2.yml");
		}
	}
}
