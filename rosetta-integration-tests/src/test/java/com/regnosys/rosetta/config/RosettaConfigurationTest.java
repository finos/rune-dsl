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
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider;

public class RosettaConfigurationTest {
	@Test
	public void testConfig() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
				return MyConfigFileProvider.class;
			}
		});
		RosettaConfiguration config = injector.getInstance(RosettaConfiguration.class);
		
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
		
	}
	

	
	private static class MyConfigFileProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-config-test.yml");
		}
	}
}
