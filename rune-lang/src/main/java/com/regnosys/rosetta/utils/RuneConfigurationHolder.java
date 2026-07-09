package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.config.file.FileBasedRuneConfigurationProvider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Holds the current {@link RuneConfiguration}, loaded from {@code rune-config.yml}.
 * <p>
 * Consumers should read the configuration through this holder (rather than capturing a
 * {@link RuneConfiguration} value at injection time) so that, when the configuration file changes,
 * a call to {@link #reload()} makes the new configuration visible to everyone. The IDE language
 * server watches the config file and triggers a reload; in non-watching contexts the configuration
 * is simply loaded once.
 */
@Singleton
public class RuneConfigurationHolder implements Provider<RuneConfiguration>, javax.inject.Provider<RuneConfiguration> {
	private final FileBasedRuneConfigurationProvider source;
	private volatile RuneConfiguration current;

	@Inject
	public RuneConfigurationHolder(FileBasedRuneConfigurationProvider source) {
		this.source = source;
	}

	@Override
	public RuneConfiguration get() {
		RuneConfiguration config = current;
		if (config == null) {
			synchronized (this) {
				config = current;
				if (config == null) {
					config = source.get();
					current = config;
				}
			}
		}
		return config;
	}

	/**
	 * Re-reads the configuration file, making the new configuration visible to all consumers that
	 * read through this holder.
	 */
	public void reload() {
		current = source.get();
	}

	/**
	 * Bound as the provider for {@link RuneConfiguration} to make direct injection fail fast:
	 * a directly injected value is captured once at injection time and silently goes stale when the
	 * configuration is {@link #reload() reloaded}. Inject {@link RuneConfigurationHolder} and call
	 * {@link #get()} at use time instead.
	 */
	public static class DirectInjectionGuard implements Provider<RuneConfiguration>, javax.inject.Provider<RuneConfiguration> {
		@Override
		public RuneConfiguration get() {
			throw new IllegalStateException(
					"RuneConfiguration must not be injected directly: the injected value is captured once and"
							+ " goes stale when the configuration is reloaded (e.g. on rosetta/updateConfig)."
							+ " Inject RuneConfigurationHolder and call get() at use time instead.");
		}
	}
}
