package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		RuneConfigurationHolder holder = holderOver(source);

		assertEquals("Before", holder.get().getModel().getName());

		// A change to the underlying config is not visible until reload() is called.
		source.set(configNamed("After"));
		assertEquals("Before", holder.get().getModel().getName());

		holder.reload();
		assertEquals("After", holder.get().getModel().getName());
	}

	@Test
	public void anOverlayAppliesAnyChangeAndGoesWhenTheScopeCloses() {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));

		try (RuneConfigurationHolder.Scope scope = holder.overlay(config -> config.toBuilder()
				.model(new RuneModelConfiguration("Overlaid", Collections.emptyList()))
				.build())) {
			assertEquals("Overlaid", holder.get().getModel().getName());
		}

		assertEquals("Base", holder.get().getModel().getName());
	}

	@Test
	public void overlaidEntriesAreUpsertedAndGoWhenTheScopeCloses() {
		RuneConfiguration configured = configNamed("Base", schema("kept", "kept.json"));
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configured));

		List<RuneNamespaceConfiguration> generated =
				Arrays.asList(schema("kept", "regenerated.json"), schema("added", "added.json"));
		try (RuneConfigurationHolder.Scope scope = holder.overlayNamespaceConfigs(generated)) {
			assertEquals("added.json", holder.get().findSchemaConfig("added").get().getConfigPath());
			// Upsert by id: a second import of the same schema updates its entry rather than adding one.
			assertEquals("regenerated.json", holder.get().findSchemaConfig("kept").get().getConfigPath());
			assertEquals("Base", holder.get().getModel().getName());
		}

		assertFalse(holder.get().findSchemaConfig("added").isPresent());
		assertEquals("kept.json", holder.get().findSchemaConfig("kept").get().getConfigPath());
	}

	@Test
	public void anOverlaySeesReloadsUnderneathIt() {
		AtomicReference<RuneConfiguration> source = new AtomicReference<>(configNamed("Before"));
		RuneConfigurationHolder holder = holderOver(source);

		try (RuneConfigurationHolder.Scope scope =
				holder.overlayNamespaceConfigs(Collections.singletonList(schema("added", "added.json")))) {
			assertEquals("Before", holder.get().getModel().getName());

			// The entries are applied to the loaded configuration on each read rather than to a snapshot
			// of it, so a reload inside the scope is picked up and still carries them.
			source.set(configNamed("After"));
			holder.reload();
			assertEquals("After", holder.get().getModel().getName());
			assertEquals("added.json", holder.get().findSchemaConfig("added").get().getConfigPath());
		}
	}

	@Test
	public void anOverlayIsInvisibleToAnotherThread() throws Exception {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));
		AtomicReference<Boolean> seenElsewhere = new AtomicReference<>();

		try (RuneConfigurationHolder.Scope scope =
				holder.overlayNamespaceConfigs(Collections.singletonList(schema("added", "added.json")))) {
			Thread other = new Thread(() -> seenElsewhere.set(holder.get().findSchemaConfig("added").isPresent()));
			other.start();
			other.join();
		}

		// Two tools overlaying at once are each validating their own work; neither should be able to
		// validate against the other's configuration.
		assertFalse(seenElsewhere.get());
	}

	@Test
	public void aSecondOverlayOnOneThreadIsRejected() {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));

		try (RuneConfigurationHolder.Scope open =
				holder.overlayNamespaceConfigs(Collections.singletonList(schema("first", "first.json")))) {
			// A second overlay means a scope that was never closed, or work that has re-entered itself;
			// either way the second would see the first one's entries.
			assertThrows(IllegalStateException.class, () ->
					holder.overlayNamespaceConfigs(Collections.singletonList(schema("second", "second.json"))));

			assertEquals("first.json", holder.get().findSchemaConfig("first").get().getConfigPath());
		}
	}

	@Test
	public void closingASpentScopeIsRejected() {
		RuneConfigurationHolder holder = holderOver(new AtomicReference<>(configNamed("Base")));
		List<RuneNamespaceConfiguration> entries = Collections.singletonList(schema("added", "added.json"));

		RuneConfigurationHolder.Scope first = holder.overlayNamespaceConfigs(entries);
		first.close();
		assertThrows(IllegalStateException.class, first::close);

		// And once another overlay is open, closing the spent one has to leave that one in force.
		RuneConfigurationHolder.Scope second = holder.overlayNamespaceConfigs(entries);
		assertThrows(IllegalStateException.class, first::close);
		assertEquals("added.json", holder.get().findSchemaConfig("added").get().getConfigPath());

		second.close();
		assertFalse(holder.get().findSchemaConfig("added").isPresent());
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

	private RuneNamespaceConfiguration schema(String id, String configPath) {
		return new RuneNamespaceConfiguration(id, id, false, new RuneOriginConfiguration("test"),
				new RuneSchemaConfiguration(id, configPath));
	}

	private RuneConfiguration configNamed(String name, RuneNamespaceConfiguration... namespaceConfig) {
		return new RuneConfiguration(
				new RuneModelConfiguration(name, Collections.emptyList()),
				Collections.emptyList(),
				new RuneGeneratorsConfiguration(),
				Arrays.asList(namespaceConfig));
	}
}
