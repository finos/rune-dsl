package com.regnosys.rosetta.config;

import java.util.function.Predicate;

import javax.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaGeneratorsConfiguration.Provider.class)
public class RosettaGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;

	public RosettaGeneratorsConfiguration(Predicate<String> namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public static class Provider implements javax.inject.Provider<RosettaGeneratorsConfiguration> {
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
