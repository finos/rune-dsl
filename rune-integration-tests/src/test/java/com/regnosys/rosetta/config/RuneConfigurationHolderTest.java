package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
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

	private RuneConfiguration configNamed(String name) {
		return new RuneConfiguration(
				new RuneModelConfiguration(name, Collections.emptyList()),
				Collections.emptyList(),
				new RuneGeneratorsConfiguration());
	}
}
