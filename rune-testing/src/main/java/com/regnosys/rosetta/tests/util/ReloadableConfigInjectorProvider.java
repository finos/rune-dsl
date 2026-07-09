package com.regnosys.rosetta.tests.util;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;

/**
 * Injector provider whose {@code rune-config.yml} source can be swapped mid-test, to exercise live
 * configuration reloads through {@link com.regnosys.rosetta.utils.RuneConfigurationHolder}.
 *
 * <p>Usage: call {@link #useConfig(String)} with a classpath resource and then
 * {@code RuneConfigurationHolder#reload()} (inject the holder) to make it visible — the holder
 * caches the configuration, so swapping the source alone has no effect. The injector is cached per
 * provider class and outlives each test, so tests must {@link #resetConfig() reset} and reload when
 * they are done. While no resource is set, the default classpath {@code rune-config.yml} lookup of
 * {@link RuneConfigurationFileProvider} applies.
 */
public class ReloadableConfigInjectorProvider extends RosettaCustomConfigInjectorProvider {

	private static final AtomicReference<String> CONFIG_RESOURCE = new AtomicReference<>(null);

	/** Serve the given classpath resource as {@code rune-config.yml}; reload the holder to see it. */
	public static void useConfig(String classpathResource) {
		CONFIG_RESOURCE.set(classpathResource);
	}

	/** Back to the default classpath {@code rune-config.yml} lookup; reload the holder to see it. */
	public static void resetConfig() {
		CONFIG_RESOURCE.set(null);
	}

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
			String resource = CONFIG_RESOURCE.get();
			if (resource == null) {
				return super.get();
			}
			return Thread.currentThread().getContextClassLoader().getResource(resource);
		}
	}
}
