package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;

public class RuneConfiguration {
	private final RuneModelConfiguration model;
	private final List<RuneDependencyConfiguration> dependencies;
	private final RuneGeneratorsConfiguration generators;
	
	public RuneConfiguration(RuneModelConfiguration model, 
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(dependencies);
		Objects.requireNonNull(generators);
		
		this.model = model;
		this.dependencies = dependencies;
		this.generators = generators;
	}

	public RuneModelConfiguration getModel() {
		return model;
	}

	public List<RuneDependencyConfiguration> getDependencies() {
		return dependencies;
	}

	public RuneGeneratorsConfiguration getGenerators() {
		return generators;
	}
}
