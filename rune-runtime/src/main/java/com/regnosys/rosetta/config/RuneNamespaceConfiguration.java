package com.regnosys.rosetta.config;

import java.util.Objects;

/**
 * Configuration for one identified unit of model content, addressed by {@code id}. It ties together
 * the aspects that apply to a namespace &mdash; whether it is read-only, and its external schema
 * configuration &mdash; so that they share a single identity and lifecycle. Each aspect is optional,
 * so the features remain usable independently (a plain read-only namespace, or a plain schema
 * configuration), while model-import can declare them together and clean them up as a unit.
 */
public class RuneNamespaceConfiguration {
	private final String id;
	private final String namespace;
	private final boolean readOnly;
	private final RuneSchemaConfiguration schemaConfig;

	public RuneNamespaceConfiguration(String id, String namespace, boolean readOnly, RuneSchemaConfiguration schemaConfig) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(namespace);
		this.id = id;
		this.namespace = namespace;
		this.readOnly = readOnly;
		this.schemaConfig = schemaConfig;
	}

	/** The identity of this unit of configuration; also the key on which configs are unioned. */
	public String getId() {
		return id;
	}

	/** The namespace (or namespace pattern) this configuration applies to. */
	public String getNamespace() {
		return namespace;
	}

	/** Whether the namespace is read-only. */
	public boolean isReadOnly() {
		return readOnly;
	}

	/** The external schema configuration for this namespace, or {@code null} if none is configured. */
	public RuneSchemaConfiguration getSchemaConfig() {
		return schemaConfig;
	}
}
