package com.regnosys.rosetta.config;

import java.util.Objects;

/**
 * Maps a serialization schema id (the name of a {@code schema} declaration) onto the
 * classpath location of its configuration file (e.g. an XML config).
 */
public class RuneSerializationConfiguration {
	private final String id;
	private final String configPath;

	public RuneSerializationConfiguration(String id, String configPath) {
		Objects.requireNonNull(id);
		this.id = id;
		this.configPath = configPath;
	}

	public String getId() {
		return id;
	}

	public String getConfigPath() {
		return configPath;
	}
}
