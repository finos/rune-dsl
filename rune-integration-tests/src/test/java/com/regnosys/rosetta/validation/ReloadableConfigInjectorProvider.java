package com.regnosys.rosetta.validation;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;
import com.regnosys.rosetta.tests.util.RosettaCustomConfigInjectorProvider;

/**
 * Injector provider whose {@code rune-config.yml} source can be swapped mid-test, to exercise
 * live configuration reloads through {@link com.regnosys.rosetta.utils.RuneConfigurationHolder}.
 * Tests must reset {@link #CONFIG_RESOURCE} to {@link #INITIAL_CONFIG} when they are done: the
 * injector is cached per provider class and outlives each test.
 */
public class ReloadableConfigInjectorProvider extends RosettaCustomConfigInjectorProvider {

	public static final String INITIAL_CONFIG = "schema-reload/rune-config-without-schema.yml";

	public static final AtomicReference<String> CONFIG_RESOURCE = new AtomicReference<>(INITIAL_CONFIG);

	@Override
	protected RosettaRuntimeModule createRuntimeModule() {
		return new RosettaRuntimeModule() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return ReloadableConfigInjectorProvider.class.getClassLoader();
			}

			@SuppressWarnings("unused")
			public Class<? extends RuneConfigurationFileProvider> bindRuneConfigurationFileProvider() {
				return SwappableConfigFileProvider.class;
			}
		};
	}

	public static class SwappableConfigFileProvider extends RuneConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource(CONFIG_RESOURCE.get());
		}
	}
}
