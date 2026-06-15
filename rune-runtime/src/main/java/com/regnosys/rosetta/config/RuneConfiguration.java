package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RuneConfiguration {
	private final RuneModelConfiguration model;
	private final List<RuneDependencyConfiguration> dependencies;
	private final RuneGeneratorsConfiguration generators;
	private final List<String> readOnlyNamespaces;
	private final List<RuneSerializationConfiguration> serializationConfig;

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators) {
		this(model, dependencies, generators, Collections.emptyList(), Collections.emptyList());
	}

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators,
			List<String> readOnlyNamespaces,
			List<RuneSerializationConfiguration> serializationConfig) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(dependencies);
		Objects.requireNonNull(generators);
		Objects.requireNonNull(readOnlyNamespaces);
		Objects.requireNonNull(serializationConfig);

		this.model = model;
		this.dependencies = dependencies;
		this.generators = generators;
		this.readOnlyNamespaces = readOnlyNamespaces;
		this.serializationConfig = serializationConfig;
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

	public List<RuneSerializationConfiguration> getSerializationConfig() {
		return serializationConfig;
	}

	/**
	 * Finds the serialization configuration with the given schema id, if any.
	 */
	public Optional<RuneSerializationConfiguration> findSerializationConfigById(String id) {
		return serializationConfig.stream()
				.filter(c -> c.getId().equals(id))
				.findFirst();
	}
}
