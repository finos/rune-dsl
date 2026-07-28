package com.regnosys.rosetta.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The external serialization configuration for a {@code schema} declaration: the name of the
 * {@code schema} and the classpath location of its configuration file (e.g. an XML config).
 */
public class RuneSchemaConfiguration {
	private final String schema;
	private final String configPath;

	@JsonCreator
	public RuneSchemaConfiguration(
			@JsonProperty("schema") String schema,
			@JsonProperty("configPath") String configPath) {
		Objects.requireNonNull(schema);
		this.schema = schema;
		this.configPath = configPath;
	}

	/** The name of the {@code schema} declaration this configuration applies to. */
	public String getSchema() {
		return schema;
	}

	/** The classpath location of the schema's configuration file; may be {@code null}. */
	public String getConfigPath() {
		return configPath;
	}
}
