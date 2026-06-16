package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RuneConfiguration {
	private final RuneModelConfiguration model;
	private final List<RuneDependencyConfiguration> dependencies;
	private final RuneGeneratorsConfiguration generators;
	private final List<String> readOnlyNamespaces;

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators) {
		this(model, dependencies, generators, Collections.emptyList());
	}

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators,
			List<String> readOnlyNamespaces) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(dependencies);
		Objects.requireNonNull(generators);
		Objects.requireNonNull(readOnlyNamespaces);

		this.model = model;
		this.dependencies = dependencies;
		this.generators = generators;
		this.readOnlyNamespaces = readOnlyNamespaces;
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

	public List<String> getReadOnlyNamespaces() {
		return readOnlyNamespaces;
	}
}
