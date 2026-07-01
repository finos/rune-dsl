package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RuneConfiguration {
	private final RuneModelConfiguration model;
	private final List<RuneDependencyConfiguration> dependencies;
	private final RuneGeneratorsConfiguration generators;
	private final List<RuneNamespaceConfiguration> namespaceConfig;

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators) {
		this(model, dependencies, generators, Collections.emptyList());
	}

	public RuneConfiguration(RuneModelConfiguration model,
			List<RuneDependencyConfiguration> dependencies,
			RuneGeneratorsConfiguration generators,
			List<RuneNamespaceConfiguration> namespaceConfig) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(dependencies);
		Objects.requireNonNull(generators);
		Objects.requireNonNull(namespaceConfig);

		this.model = model;
		this.dependencies = dependencies;
		this.generators = generators;
		this.namespaceConfig = namespaceConfig;
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

	public List<RuneNamespaceConfiguration> getNamespaceConfig() {
		return namespaceConfig;
	}

	/**
	 * The namespaces (or namespace patterns) configured as read-only.
	 */
	public List<String> getReadOnlyNamespaces() {
		return namespaceConfig.stream()
				.filter(RuneNamespaceConfiguration::isReadOnly)
				.map(RuneNamespaceConfiguration::getNamespace)
				.collect(Collectors.toList());
	}

	/**
	 * Finds the external schema configuration for the given schema name, if any.
	 */
	public Optional<RuneSchemaConfiguration> findSchemaConfig(String schema) {
		return namespaceConfig.stream()
				.map(RuneNamespaceConfiguration::getSchemaConfig)
				.filter(Objects::nonNull)
				.filter(c -> c.getSchema().equals(schema))
				.findFirst();
	}
}
