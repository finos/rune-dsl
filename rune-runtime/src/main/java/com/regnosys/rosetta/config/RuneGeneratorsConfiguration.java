package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class RuneGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;
	private final List<RuneAttributeReference> doNotPrune;

	public RuneGeneratorsConfiguration() {
		this(n -> true, Collections.emptyList());
	}
	public RuneGeneratorsConfiguration(Predicate<String> namespaceFilter, List<RuneAttributeReference> doNotPrune) {
		Objects.requireNonNull(namespaceFilter);
		Objects.requireNonNull(doNotPrune);
		this.namespaceFilter = namespaceFilter;
		this.doNotPrune = doNotPrune;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public List<RuneAttributeReference> doNotPrune() {
		return doNotPrune;
	}
}
