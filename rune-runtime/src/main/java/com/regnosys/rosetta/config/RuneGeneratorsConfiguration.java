package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RuneGeneratorsConfiguration.Provider.class)
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

	public static class Provider implements javax.inject.Provider<RuneGeneratorsConfiguration> {
		private final RuneConfiguration config;
		@Inject
		public Provider(RuneConfiguration config) {
			this.config = config;
		}
		
		@Override
		public RuneGeneratorsConfiguration get() {
			return config.getGenerators();
		}
	}
}
