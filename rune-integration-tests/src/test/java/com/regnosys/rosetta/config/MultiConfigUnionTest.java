package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;

/**
 * Verifies that serializationConfig is the union of all configs on the classpath, with the current
 * project (the primary config returned by {@link RuneConfigurationFileProvider#get()}) shadowing
 * dependency configs on id collisions, while the model still comes from the current project only.
 */
public class MultiConfigUnionTest {

	@Test
	public void serializationConfigIsUnionedWithCurrentProjectFirst() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RuneConfigurationFileProvider> bindRuneConfigurationFileProvider() {
				return UnionConfigFileProvider.class;
			}
		});
		RuneConfiguration config = injector.getInstance(RuneConfiguration.class);

		// Model comes from the current project (primary) config only.
		assertEquals("XYZ Model", config.getModel().getName());

		// myXmlSchema is defined in both; the current project's config path wins.
		assertEquals("xml-config/my-xml-schema-config.json",
				config.findSerializationConfigById("myXmlSchema").orElseThrow().getConfigPath());
		// myJson is defined only in the current project.
		assertEquals("json-config/my-json-config.json",
				config.findSerializationConfigById("myJson").orElseThrow().getConfigPath());
		// myOtherSchema is defined only in the dependency config and is included in the union.
		assertEquals("xml-config/my-other-schema-config.json",
				config.findSerializationConfigById("myOtherSchema").orElseThrow().getConfigPath());

		assertEquals(3, config.getSerializationConfig().size());
	}

	private static class UnionConfigFileProvider extends RuneConfigurationFileProvider {
		private static final ClassLoader CL = Thread.currentThread().getContextClassLoader();

		@Override
		public URL get() {
			return CL.getResource("rune-config-test.yml");
		}

		@Override
		public Collection<URL> getResources() {
			return List.of(CL.getResource("rune-config-test.yml"), CL.getResource("rune-config-dependency.yml"));
		}
	}
}
