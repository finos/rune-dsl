package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;

public class RosettaConfiguration {
	private final RosettaModelConfiguration model;
	private final List<RosettaDependencyConfiguration> dependencies;
	private final RosettaGeneratorsConfiguration generators;
	
	public RosettaConfiguration(RosettaModelConfiguration model, 
			List<RosettaDependencyConfiguration> dependencies,
			RosettaGeneratorsConfiguration generators) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(dependencies);
		Objects.requireNonNull(generators);
		
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
