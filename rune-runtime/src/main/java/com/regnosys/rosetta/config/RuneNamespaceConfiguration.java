package com.regnosys.rosetta.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for one unit of model content applying to a namespace. It ties together the aspects
 * that apply to that namespace &mdash; whether it is read-only, and its external schema configuration
 * &mdash; so that they share a single identity and lifecycle. Each aspect is optional, so the features
 * remain usable independently (a plain read-only namespace, or a plain schema configuration), while
 * model-import can declare them together and clean them up as a unit.
 * <p>
 * The optional {@code id} gives the entry a stable identity: when present it is the key on which
 * configs are unioned across a project and its dependencies (so a re-declared entry replaces rather
 * than duplicates). It is not required &mdash; e.g. a plain read-only namespace needs no id.
 */
public class RuneNamespaceConfiguration {
	private final String id;
	private final String namespace;
	private final boolean readOnly;
	private final RuneSchemaConfiguration schemaConfig;

	@JsonCreator
	public RuneNamespaceConfiguration(
			@JsonProperty("id") String id,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("readOnly") boolean readOnly,
			@JsonProperty("schemaConfig") RuneSchemaConfiguration schemaConfig) {
		Objects.requireNonNull(namespace);
		this.id = id;
		this.namespace = namespace;
		this.readOnly = readOnly;
		this.schemaConfig = schemaConfig;
	}

	/**
	 * The optional identity of this unit of configuration; when present, the key on which configs are
	 * unioned across a project and its dependencies. May be {@code null}.
	 */
	public String getId() {
		return id;
	}

	/** The namespace (or namespace pattern) this configuration applies to. */
	public String getNamespace() {
		return namespace;
	}

	/** Whether the namespace is read-only. Omitted from serialization when {@code false}. */
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public boolean isReadOnly() {
		return readOnly;
	}

	/** The external schema configuration for this namespace, or {@code null} if none is configured. */
	public RuneSchemaConfiguration getSchemaConfig() {
		return schemaConfig;
	}
}
