package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import jakarta.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaGeneratorsConfiguration.Provider.class)
public class RosettaGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;
	private final RosettaTabulatorConfiguration rosettaTabulatorConfiguration;

	public RosettaGeneratorsConfiguration() {
		this(n -> true, new RosettaTabulatorConfiguration(List.of(), List.of()));
	}
	public RosettaGeneratorsConfiguration(Predicate<String> namespaceFilter, RosettaTabulatorConfiguration tabulators) {
		Objects.requireNonNull(namespaceFilter);
		Objects.requireNonNull(tabulators);
		
		this.namespaceFilter = namespaceFilter;
		this.rosettaTabulatorConfiguration = tabulators;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public RosettaTabulatorConfiguration getTabulators() {
		return rosettaTabulatorConfiguration;
	}

	public static class Provider implements jakarta.inject.Provider<RosettaGeneratorsConfiguration> {
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
