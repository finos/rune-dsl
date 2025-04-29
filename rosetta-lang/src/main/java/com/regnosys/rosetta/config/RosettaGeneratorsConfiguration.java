package com.regnosys.rosetta.config;

import java.util.Objects;
import java.util.function.Predicate;

import jakarta.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaGeneratorsConfiguration.Provider.class)
public class RosettaGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;

	public RosettaGeneratorsConfiguration() {
		this(n -> true);
	}
	public RosettaGeneratorsConfiguration(Predicate<String> namespaceFilter) {
		Objects.requireNonNull(namespaceFilter);
		this.namespaceFilter = namespaceFilter;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
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
