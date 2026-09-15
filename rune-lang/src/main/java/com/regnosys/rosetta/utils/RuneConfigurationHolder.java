package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.config.RuneNamespaceConfiguration;
import com.regnosys.rosetta.config.file.FileBasedRuneConfigurationProvider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

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
 * the configuration for the duration of that work with {@link #overlay}, without touching the file or
 * what any other thread sees.
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
	 * Applies {@code extra} to what {@link #get()} returns, on the calling thread only, until the
	 * returned scope is closed. Meant for a try-with-resources around work whose configuration is not
	 * on disk yet.
	 * <p>
	 * The function runs on each {@link #get()} rather than once, so a {@link #reload()} inside the
	 * scope is picked up and still carries the overlay. Work handed to another thread does not see it,
	 * and a second overlay on this thread throws, because it would see this one's changes.
	 *
	 * @param extra applied to the loaded configuration; must not be null
	 * @return the scope to close, which is what takes the overlay away again
	 * @throws IllegalStateException if this thread already has an overlay open
	 */
	public Scope overlay(UnaryOperator<RuneConfiguration> extra) {
		Objects.requireNonNull(extra, "An overlay must be a function of the loaded configuration, not null.");
		if (overlay.get() != null) {
			throw new IllegalStateException(
					"This thread already has a configuration overlay open. Close it before opening another.");
		}
		Scope scope = new Scope(extra);
		overlay.set(scope);
		return scope;
	}

	/**
	 * Adds {@code entries} to the configured namespaces through {@link #overlay}, which is the case a
	 * generator has: it has just produced a namespace and has to validate against a configuration for
	 * it. Each entry is upserted by its id, and every other configured namespace still applies.
	 *
	 * @param entries the namespace configurations to add; must not be null
	 * @return the scope to close, which is what takes the entries away again
	 */
	public Scope overlayNamespaceConfigs(List<RuneNamespaceConfiguration> entries) {
		Objects.requireNonNull(entries, "Namespace configurations to overlay must be a list, not null.");
		List<RuneNamespaceConfiguration> added = List.copyOf(entries);
		return overlay(config -> {
			RuneConfiguration.Builder builder = config.toBuilder();
			added.forEach(builder::addNamespaceConfig);
			return builder.build();
		});
	}

	/** One {@link #overlay} in force, closed when the work it covers is over. */
	public final class Scope implements AutoCloseable {
		private final UnaryOperator<RuneConfiguration> extra;

		private Scope(UnaryOperator<RuneConfiguration> extra) {
			this.extra = extra;
		}

		private RuneConfiguration applyTo(RuneConfiguration config) {
			return extra.apply(config);
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
