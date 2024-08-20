package com.regnosys.rosetta.config;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;

public class RosettaTabulatorConfiguration {
	private final List<String> annotations;

	public RosettaTabulatorConfiguration() {
		this(Collections.emptyList());
	}
	public RosettaTabulatorConfiguration(List<String> annotations) {
		Validate.noNullElements(annotations);
		
		this.annotations = annotations;
	}

	public List<String> getAnnotations() {
		return annotations;
	}
	
	public static class Provider  implements javax.inject.Provider<RosettaTabulatorConfiguration> {
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
