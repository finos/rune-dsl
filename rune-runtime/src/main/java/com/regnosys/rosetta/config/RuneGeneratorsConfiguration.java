package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RuneGeneratorsConfiguration {
	private final List<String> namespaces;
	private final List<RuneAttributeReference> doNotPrune;

	// Derived from `namespaces`; precomputed once, never serialized.
	@JsonIgnore
	private final transient Predicate<String> namespaceFilter;

	public RuneGeneratorsConfiguration() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	@JsonCreator
	public RuneGeneratorsConfiguration(
			@JsonProperty("namespaces") List<String> namespaces,
			@JsonProperty("doNotPrune") List<RuneAttributeReference> doNotPrune) {
		this.namespaces = namespaces == null ? Collections.emptyList() : namespaces;
		this.doNotPrune = doNotPrune == null ? Collections.emptyList() : doNotPrune;
		this.namespaceFilter = buildNamespaceFilter(this.namespaces);
	}

	/** The namespaces (or namespace patterns, e.g. {@code abc.def.*}) that generation is limited to. */
	public List<String> getNamespaces() {
		return namespaces;
	}

	public List<RuneAttributeReference> doNotPrune() {
		return doNotPrune;
	}

	/** Whether this holds no generator configuration, in which case it is omitted from serialization. */
	@JsonIgnore
	public boolean isEmpty() {
		return namespaces.isEmpty() && doNotPrune.isEmpty();
	}

	/**
	 * Jackson {@code valueFilter} that omits an empty generators section, so a roundtrip writes no
	 * {@code generators: {}} noise.
	 */
	public static final class EmptyFilter {
		@Override
		public boolean equals(Object other) {
			return other instanceof RuneGeneratorsConfiguration && ((RuneGeneratorsConfiguration) other).isEmpty();
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	/**
	 * A predicate testing whether code is generated for a namespace, derived from
	 * {@link #getNamespaces()}. An empty list places no restriction and so matches everything; otherwise
	 * the patterns are interpreted by {@link RuneNamespacePattern}.
	 */
	@JsonIgnore
	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}

	private static Predicate<String> buildNamespaceFilter(List<String> namespaces) {
		// No configured namespaces means generation is not restricted, which is specific to generators:
		// everywhere else an empty pattern list matches nothing.
		if (namespaces.isEmpty()) {
			return name -> true;
		}
		return RuneNamespacePattern.anyOf(namespaces);
	}
}
