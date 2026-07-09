package com.regnosys.rosetta.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rosetta.util.DottedPath;

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
	 * A predicate testing whether a namespace is generated, derived from {@link #getNamespaces()}: an
	 * empty list matches everything, an {@code abc.*} pattern matches that prefix, and a plain
	 * {@code abc.def} matches exactly.
	 */
	@JsonIgnore
	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}

	private static Predicate<String> buildNamespaceFilter(List<String> namespaces) {
		if (namespaces.isEmpty()) {
			return n -> true;
		}
		List<DottedPath> genericNamespaces = new ArrayList<>(); // of the form `abc.efg.*`
		List<DottedPath> specificNamespaces = new ArrayList<>(); // of the form `abc.efg`
		for (String pattern : namespaces) {
			DottedPath namespacePattern = DottedPath.splitOnDots(pattern);
			if (namespacePattern.last().equals("*")) {
				genericNamespaces.add(namespacePattern.parent());
			} else {
				specificNamespaces.add(namespacePattern);
			}
		}
		return name -> {
			DottedPath namespace = DottedPath.splitOnDots(name);
			return genericNamespaces.stream().anyMatch(namespace::startsWith) || specificNamespaces.contains(namespace);
		};
	}
}
