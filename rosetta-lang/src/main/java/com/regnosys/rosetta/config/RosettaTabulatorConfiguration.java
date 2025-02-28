package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;

public class RosettaTabulatorConfiguration {
	private final List<String> annotations;
	private final List<String> types;

	public RosettaTabulatorConfiguration() {
		this(Collections.emptyList(), Collections.emptyList());
	}
	public RosettaTabulatorConfiguration(List<String> annotations, List<String> types) {
		Validate.noNullElements(annotations);
		Validate.noNullElements(types);
		
		this.annotations = annotations;
		this.types = types;
	}

	public List<String> getAnnotations() {
		return annotations;
	}
	
	public List<String> getTypes() {
		return types;
	}

	public static class Provider  implements jakarta.inject.Provider<RosettaTabulatorConfiguration>, javax.inject.Provider<RosettaTabulatorConfiguration> {
		private final RosettaConfiguration config;
		@Inject
		public Provider(RosettaConfiguration config) {
			this.config = config;
		}
		
		@Override
		public RosettaTabulatorConfiguration get() {
			return config.getGenerators().getTabulators();
		}
	}
}
