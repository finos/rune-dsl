package com.regnosys.rosetta.config;

import java.util.Collections;

import javax.inject.Provider;

public class DefaultRuneConfigurationProvider implements Provider<RuneConfiguration> {
	@Override
	public RuneConfiguration get() {
		return new RuneConfiguration(
					new RuneModelConfiguration("Just another Rosetta model", Collections.emptyList()),
					Collections.emptyList(),
					new RuneGeneratorsConfiguration()
				);
	}
}
