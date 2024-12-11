package com.regnosys.rosetta.config;

import javax.inject.Inject;

import com.google.inject.ProvidedBy;

@ProvidedBy(JavaConfiguration.Provider.class)
public class JavaConfiguration {
	private final String runtimeModuleClass;

	public JavaConfiguration(String runtimeModuleClass) {
		this.runtimeModuleClass = runtimeModuleClass;
	}

	public String getRuntimeModuleClass() {
		return runtimeModuleClass;
	}
	
	public static class Provider implements javax.inject.Provider<JavaConfiguration> {
		private final RosettaConfiguration config;
		@Inject
		public Provider(RosettaConfiguration config) {
			this.config = config;
		}
		
		@Override
		public JavaConfiguration get() {
			return config.getGenerators().getJava();
		}
		
	}
}