package com.regnosys.rosetta.config;

import javax.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(RosettaModelConfiguration.Provider.class)
public class RosettaModelConfiguration {
	private final String name;

	public RosettaModelConfiguration(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static class Provider implements javax.inject.Provider<RosettaModelConfiguration> {
		private final RosettaConfiguration config;
		@Inject
		public Provider(RosettaConfiguration config) {
			this.config = config;
		}
		
		@Override
		public RosettaModelConfiguration get() {
			return config.getModel();
		}
		
	}
}
