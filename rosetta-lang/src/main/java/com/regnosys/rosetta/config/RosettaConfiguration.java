package com.regnosys.rosetta.config;

import java.util.List;

public class RosettaConfiguration {
	private final RosettaModelConfiguration model;
	private final List<RosettaDependencyConfiguration> dependencies;
	private final RosettaGeneratorsConfiguration generators;
	
	public RosettaConfiguration(RosettaModelConfiguration model, 
			List<RosettaDependencyConfiguration> dependencies,
			RosettaGeneratorsConfiguration generators) {
		this.model = model;
		this.dependencies = dependencies;
		this.generators = generators;
	}

	public RosettaModelConfiguration getModel() {
		return model;
	}

	public List<RosettaDependencyConfiguration> getDependencies() {
		return dependencies;
	}

	public RosettaGeneratorsConfiguration getGenerators() {
		return generators;
	}
}
