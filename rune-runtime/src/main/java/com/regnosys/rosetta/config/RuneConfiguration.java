package com.regnosys.rosetta.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonCreator
	public RuneConfiguration(
			@JsonProperty("model") RuneModelConfiguration model,
			@JsonProperty("dependencies") List<RuneDependencyConfiguration> dependencies,
			@JsonProperty("generators") RuneGeneratorsConfiguration generators,
			@JsonProperty("namespaceConfig") List<RuneNamespaceConfiguration> namespaceConfig) {
		Objects.requireNonNull(model);
		this.model = model;
		this.dependencies = dependencies == null ? Collections.emptyList() : dependencies;
		this.generators = generators == null ? new RuneGeneratorsConfiguration() : generators;
		this.namespaceConfig = namespaceConfig == null ? Collections.emptyList() : namespaceConfig;
	}

	public RuneModelConfiguration getModel() {
		return model;
	}

	public List<RuneDependencyConfiguration> getDependencies() {
		return dependencies;
	}

	@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = RuneGeneratorsConfiguration.EmptyFilter.class)
	public RuneGeneratorsConfiguration getGenerators() {
		return generators;
	}

	public List<RuneNamespaceConfiguration> getNamespaceConfig() {
		return namespaceConfig;
	}

	/**
	 * The namespaces (or namespace patterns) configured as read-only. Derived from the read-only
	 * {@code namespaceConfig} entries, so it is never serialized.
	 */
	@JsonIgnore
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

	/** A builder seeded with this configuration's values, for deriving a modified copy. */
	public Builder toBuilder() {
		return new Builder(this);
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builds a {@link RuneConfiguration} incrementally, so callers can derive a modified copy without
	 * restating every part. {@link RuneConfiguration} itself stays immutable.
	 */
	public static final class Builder {
		private RuneModelConfiguration model;
		private List<RuneDependencyConfiguration> dependencies = new ArrayList<>();
		private RuneGeneratorsConfiguration generators = new RuneGeneratorsConfiguration();
		private List<RuneNamespaceConfiguration> namespaceConfig = new ArrayList<>();

		private Builder() {
		}

		private Builder(RuneConfiguration config) {
			this.model = config.model;
			this.dependencies = new ArrayList<>(config.dependencies);
			this.generators = config.generators;
			this.namespaceConfig = new ArrayList<>(config.namespaceConfig);
		}

		public Builder model(RuneModelConfiguration model) {
			this.model = model;
			return this;
		}

		public Builder dependencies(List<RuneDependencyConfiguration> dependencies) {
			this.dependencies = new ArrayList<>(dependencies);
			return this;
		}

		public Builder generators(RuneGeneratorsConfiguration generators) {
			this.generators = generators;
			return this;
		}

		public Builder namespaceConfig(List<RuneNamespaceConfiguration> namespaceConfig) {
			this.namespaceConfig = new ArrayList<>(namespaceConfig);
			return this;
		}

		/**
		 * Adds a namespace configuration. If the entry has an {@code id}, any existing entry with the
		 * same id is replaced (upsert) — the common case for model import, where re-importing a schema
		 * updates its entry rather than duplicating it. An entry without an id is simply appended.
		 */
		public Builder addNamespaceConfig(RuneNamespaceConfiguration entry) {
			Objects.requireNonNull(entry);
			if (entry.getId() != null) {
				namespaceConfig.removeIf(existing -> entry.getId().equals(existing.getId()));
			}
			namespaceConfig.add(entry);
			return this;
		}

		public RuneConfiguration build() {
			return new RuneConfiguration(model, dependencies, generators, namespaceConfig);
		}
	}
}
