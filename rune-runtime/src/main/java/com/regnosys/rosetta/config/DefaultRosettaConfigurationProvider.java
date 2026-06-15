package com.regnosys.rosetta.config;

import java.util.Collections;

import javax.inject.Provider;

public class DefaultRosettaConfigurationProvider implements Provider<RosettaConfiguration> {
	@Override
	public RosettaConfiguration get() {
		return new RosettaConfiguration(
					new RosettaModelConfiguration("Just another Rosetta model", Collections.emptyList()),
					Collections.emptyList(),
					new RosettaGeneratorsConfiguration()
				);
	}
}
