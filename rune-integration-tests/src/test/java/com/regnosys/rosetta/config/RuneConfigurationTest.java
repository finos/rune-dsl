package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;

public class RuneConfigurationTest {
	@Test
	public void testConfig() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RuneConfigurationFileProvider> bindRuneConfigurationFileProvider() {
				return MyConfigFileProvider.class;
			}
		});
		RuneConfiguration config = injector.getInstance(RuneConfiguration.class);
		
		assertNotNull(config.getModel());
		assertEquals("XYZ Model", config.getModel().getName());
		assertEquals(Collections.emptyList(), config.getDependencies());
		assertNotNull(config.getGenerators());
		
		Predicate<String> filter = config.getGenerators().getNamespaceFilter();
		assertNotNull(filter);
		assertTrue(filter.test("xyz"));
		assertTrue(filter.test("xyz.sub"));
		assertFalse(filter.test("xyzsub"));
		assertFalse(filter.test("foo"));
		assertTrue(filter.test("abc.def"));
		assertFalse(filter.test("abc.def.sub"));

		assertEquals(java.util.List.of("com.rosetta.model.*", "abc.def"), config.getReadOnlyNamespaces());
	}
	

	
	private static class MyConfigFileProvider extends RuneConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rune-config-test.yml");
		}
	}
}
