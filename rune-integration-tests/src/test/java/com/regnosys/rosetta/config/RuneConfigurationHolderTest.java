package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.config.file.FileBasedRuneConfigurationProvider;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;
import com.regnosys.rosetta.utils.RuneConfigurationHolder;

public class RuneConfigurationHolderTest {

	@Test
	public void cachesValueAndPicksUpChangesOnReload() {
		AtomicReference<RuneConfiguration> source = new AtomicReference<>(configNamed("Before"));
		RuneConfigurationHolder holder = new RuneConfigurationHolder(
				new FileBasedRuneConfigurationProvider(new DefaultRuneConfigurationProvider(), new RuneConfigurationFileProvider()) {
					@Override
					public RuneConfiguration get() {
						return source.get();
					}
				});

		assertEquals("Before", holder.get().getModel().getName());

		// A change to the underlying config is not visible until reload() is called.
		source.set(configNamed("After"));
		assertEquals("Before", holder.get().getModel().getName());

		holder.reload();
		assertEquals("After", holder.get().getModel().getName());
	}

	@Test
	public void anOverlayAppliesUntilItsScopeCloses() {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));

		try (RuneConfigurationHolder.Scope scope = holder.overlay(config -> configNamed("Overlaid"))) {
			assertEquals("Overlaid", holder.get().getModel().getName());
		}

		assertEquals("Base", holder.get().getModel().getName());
	}

	@Test
	public void anOverlaySeesReloadsUnderneathIt() {
		AtomicReference<RuneConfiguration> source = new AtomicReference<>(configNamed("Before"));
		RuneConfigurationHolder holder = holderOver(source);

		try (RuneConfigurationHolder.Scope scope =
				holder.overlay(config -> configNamed(config.getModel().getName() + "+overlay"))) {
			assertEquals("Before+overlay", holder.get().getModel().getName());

			// The overlay is a function of the loaded configuration, not a snapshot of it, so a reload
			// inside the scope is picked up and still carries the overlay.
			source.set(configNamed("After"));
			holder.reload();
			assertEquals("After+overlay", holder.get().getModel().getName());
		}

		assertEquals("After", holder.get().getModel().getName());
	}

	@Test
	public void overlaysNestAndUnwindInOrder() {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));

		try (RuneConfigurationHolder.Scope outer =
				holder.overlay(config -> configNamed(config.getModel().getName() + "+outer"))) {
			assertEquals("Base+outer", holder.get().getModel().getName());

			try (RuneConfigurationHolder.Scope inner =
					holder.overlay(config -> configNamed(config.getModel().getName() + "+inner"))) {
				assertEquals("Base+outer+inner", holder.get().getModel().getName());
			}

			assertEquals("Base+outer", holder.get().getModel().getName());
		}

		assertEquals("Base", holder.get().getModel().getName());
	}

	@Test
	public void anOverlayIsInvisibleToAnotherThread() throws Exception {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));
		AtomicReference<String> seenElsewhere = new AtomicReference<>();

		try (RuneConfigurationHolder.Scope scope = holder.overlay(config -> configNamed("Overlaid"))) {
			Thread other = new Thread(() -> seenElsewhere.set(holder.get().getModel().getName()));
			other.start();
			other.join();
		}

		// Two tools overlaying at once are each validating their own work; neither should be able to
		// validate against the other's configuration.
		assertEquals("Base", seenElsewhere.get());
	}

	@Test
	public void namespaceConfigsOverlaidAreUpsertedOntoTheConfiguredOnes() {
		RuneConfiguration configured = new RuneConfiguration(
				new RuneModelConfiguration("Base", Collections.emptyList()),
				Collections.emptyList(),
				new RuneGeneratorsConfiguration(),
				Collections.singletonList(schema("kept", "kept.json")));
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configured));

		List<RuneNamespaceConfiguration> generated =
				Arrays.asList(schema("kept", "regenerated.json"), schema("added", "added.json"));
		try (RuneConfigurationHolder.Scope scope = holder.overlayNamespaceConfigs(generated)) {
			assertEquals("added.json", holder.get().findSchemaConfig("added").get().getConfigPath());
			// Upsert by id: a second import of the same schema updates its entry rather than adding one.
			assertEquals("regenerated.json", holder.get().findSchemaConfig("kept").get().getConfigPath());
		}

		assertFalse(holder.get().findSchemaConfig("added").isPresent());
		assertEquals("kept.json", holder.get().findSchemaConfig("kept").get().getConfigPath());
		assertTrue(holder.get().getModel().getName().equals("Base"));
	}

	private RuneNamespaceConfiguration schema(String id, String configPath) {
		return new RuneNamespaceConfiguration(id, id, false, new RuneOriginConfiguration("test"),
				new RuneSchemaConfiguration(id, configPath));
	}

	private RuneConfigurationHolder holderOver(AtomicReference<RuneConfiguration> source) {
		return new RuneConfigurationHolder(
				new FileBasedRuneConfigurationProvider(new DefaultRuneConfigurationProvider(), new RuneConfigurationFileProvider()) {
					@Override
					public RuneConfiguration get() {
						return source.get();
					}
				});
	}

	private RuneConfiguration configNamed(String name) {
		return new RuneConfiguration(
				new RuneModelConfiguration(name, Collections.emptyList()),
				Collections.emptyList(),
				new RuneGeneratorsConfiguration());
	}
}
