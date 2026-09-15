package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.config.RuneNamespaceConfiguration;
import com.regnosys.rosetta.config.file.FileBasedRuneConfigurationProvider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;

/**
 * Holds the current {@link RuneConfiguration}, loaded from {@code rune-config.yml}.
 * <p>
 * Consumers should read the configuration through this holder (rather than capturing a
 * {@link RuneConfiguration} value at injection time) so that, when the configuration file changes,
 * a call to {@link #reload()} makes the new configuration visible to everyone. The IDE language
 * server watches the config file and triggers a reload; in non-watching contexts the configuration
 * is simply loaded once.
 * <p>
 * A tool that generates a model and validates it before anything has been written to disk can add to
 * the configuration for the duration of that work with {@link #overlayNamespaceConfigs}, without
 * touching the file or what any other thread sees.
 */
@Singleton
public class RuneConfigurationHolder implements Provider<RuneConfiguration>, javax.inject.Provider<RuneConfiguration> {
	private final FileBasedRuneConfigurationProvider source;
	private volatile RuneConfiguration current;

	// Per-thread: two tools overlaying at once are each validating their own work, and neither should
	// see the other's.
	private final ThreadLocal<Scope> overlay = new ThreadLocal<>();

	@Inject
	public RuneConfigurationHolder(FileBasedRuneConfigurationProvider source) {
		this.source = source;
	}

	@Override
	public RuneConfiguration get() {
		RuneConfiguration config = loaded();
		Scope active = overlay.get();
		return active == null ? config : active.applyTo(config);
	}

	private RuneConfiguration loaded() {
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
	 * Adds {@code entries} to the configured namespaces, on the calling thread only, until the returned
	 * scope is closed. Each entry is upserted by its id, and every other configured namespace still
	 * applies.
	 * <p>
	 * A {@link #reload()} inside the scope keeps the entries; work handed to another thread does not
	 * see them; and a second overlay on this thread throws, because it would see this one's entries.
	 *
	 * @param entries the namespace configurations to add; must not be null
	 * @return the scope to close, which is what takes the entries away again
	 * @throws IllegalStateException if this thread already has an overlay open
	 */
	public Scope overlayNamespaceConfigs(List<RuneNamespaceConfiguration> entries) {
		Objects.requireNonNull(entries, "Namespace configurations to overlay must be a list, not null.");
		if (overlay.get() != null) {
			throw new IllegalStateException(
					"This thread already has a configuration overlay open. Close it before opening another.");
		}
		Scope scope = new Scope(List.copyOf(entries));
		overlay.set(scope);
		return scope;
	}

	/** One {@link #overlayNamespaceConfigs} in force, closed when the work it covers is over. */
	public final class Scope implements AutoCloseable {
		private final List<RuneNamespaceConfiguration> entries;

		private Scope(List<RuneNamespaceConfiguration> entries) {
			this.entries = entries;
		}

		private RuneConfiguration applyTo(RuneConfiguration config) {
			RuneConfiguration.Builder builder = config.toBuilder();
			entries.forEach(builder::addNamespaceConfig);
			return builder.build();
		}

		/** Closing a spent scope throws rather than taking away an overlay that belongs to other work. */
		@Override
		public void close() {
			if (overlay.get() != this) {
				throw new IllegalStateException("This configuration overlay is not the one in force on this"
						+ " thread. A scope is closed once: use try-with-resources.");
			}
			overlay.remove();
		}
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
