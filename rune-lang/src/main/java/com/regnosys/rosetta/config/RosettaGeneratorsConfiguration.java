package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import jakarta.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaGeneratorsConfiguration.Provider.class)
public class RosettaGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;
	private final List<RosettaAttributeReference> doNotPrune;

	public RosettaGeneratorsConfiguration() {
		this(n -> true, List.of());
	}
	public RosettaGeneratorsConfiguration(Predicate<String> namespaceFilter, List<RosettaAttributeReference> doNotPrune) {
		Objects.requireNonNull(namespaceFilter);
		Objects.requireNonNull(doNotPrune);
		this.namespaceFilter = namespaceFilter;
		this.doNotPrune = doNotPrune;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public List<RosettaAttributeReference> doNotPrune() {
		return doNotPrune;
	}

	public static class Provider implements jakarta.inject.Provider<RosettaGeneratorsConfiguration>, javax.inject.Provider<RosettaGeneratorsConfiguration> {
		private final RosettaConfiguration config;
		@Inject
		public Provider(RosettaConfiguration config) {
			this.config = config;
		}
		
		@Override
		public RosettaGeneratorsConfiguration get() {
			return config.getGenerators();
		}
	}
}
