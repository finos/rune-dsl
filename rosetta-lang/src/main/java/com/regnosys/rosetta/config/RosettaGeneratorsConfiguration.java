package com.regnosys.rosetta.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaGeneratorsConfiguration.Provider.class)
public class RosettaGeneratorsConfiguration {
	private final Predicate<String> namespaceFilter;
	private final RosettaTabulatorConfiguration rosettaTabulatorConfiguration;
	private final JavaConfiguration java;

	public RosettaGeneratorsConfiguration() {
		this(n -> true, new RosettaTabulatorConfiguration(List.of(), List.of()), new JavaConfiguration(null));
	}
	public RosettaGeneratorsConfiguration(Predicate<String> namespaceFilter, RosettaTabulatorConfiguration tabulators, JavaConfiguration java) {
		Objects.requireNonNull(namespaceFilter);
		Objects.requireNonNull(tabulators);
		Objects.requireNonNull(java);
		
		this.namespaceFilter = namespaceFilter;
		this.rosettaTabulatorConfiguration = tabulators;
		this.java = java;
	}

	public Predicate<String> getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public RosettaTabulatorConfiguration getTabulators() {
		return rosettaTabulatorConfiguration;
	}
	
	public JavaConfiguration getJava() {
		return java;
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
