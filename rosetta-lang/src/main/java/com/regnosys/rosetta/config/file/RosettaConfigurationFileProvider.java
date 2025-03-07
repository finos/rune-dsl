package com.regnosys.rosetta.config.file;

import java.net.URL;

import javax.inject.Provider;

public class RosettaConfigurationFileProvider implements Provider<URL> {	
	public static final String FILE_NAME = "rosetta-config.yml";

	@Override
	public URL get() {
		return Thread.currentThread().getContextClassLoader().getResource(FILE_NAME);
	}
}
