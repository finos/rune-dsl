package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Collections;
import java.util.List;
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
		
		assertNotNull(config.getGenerators().getTabulators());
		List<String> annotations = config.getGenerators().getTabulators().getAnnotations();
		assertEquals(2, annotations.size());
		assertTrue(annotations.contains("projection"));
		assertTrue(annotations.contains("enrich"));
		
		assertNotNull(config.getGenerators());
		assertNotNull(config.getGenerators().getJava());
		String runtimeModuleClass = config.getGenerators().getJava().getRuntimeModuleClass();
		assertEquals("com.regnosys.model.XyzRuntimeModule", runtimeModuleClass);
	}
	
	@Test
	public void testConfigWithNamespaceOnlyConfig() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
				return ConfigWithNamespaceOnlyFileProvider.class;
			}
		});
		RosettaConfiguration config = injector.getInstance(RosettaConfiguration.class);

		assertNotNull(config.getGenerators());
		assertNotNull(config.getGenerators().getTabulators());
		List<String> annotations = config.getGenerators().getTabulators().getAnnotations();
		assertNotNull(annotations);
		assertEquals(0, annotations.size());
		
		assertNotNull(config.getGenerators());
		assertNotNull(config.getGenerators().getJava());
		assertNull(config.getGenerators().getJava().getRuntimeModuleClass());
	}
	
	@Test
	public void testConfigWithEmptyRuntimeModuleClassConfig() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
				return ConfigWithEmptyRuntimeModuleClassFileProvider.class;
			}
		});
		RosettaConfiguration config = injector.getInstance(RosettaConfiguration.class);

		assertNotNull(config.getGenerators());
		assertNotNull(config.getGenerators().getJava());
		assertNull(config.getGenerators().getJava().getRuntimeModuleClass());
		
	}
	
	private static class MyConfigFileProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-config-test.yml");
		}
	}
	private static class ConfigWithNamespaceOnlyFileProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-config-test-with-namespace-only.yml");
		}
	}
	private static class ConfigWithEmptyRuntimeModuleClassFileProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-config-test-empty-runtime-module-class.yml");
		}
	}
}
