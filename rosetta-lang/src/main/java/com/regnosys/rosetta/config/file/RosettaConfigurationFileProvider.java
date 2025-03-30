package com.regnosys.rosetta.config.file;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import javax.inject.Provider;

public class RosettaConfigurationFileProvider implements Provider<URL> {	
	public static final String FILE_NAME = "rosetta-config.yml";
	private static final Logger LOGGER = LoggerFactory.getLogger(RosettaConfigurationFileProvider.class);
	public static final String ROSETTA_CONFIG_FILE_PROPERTY = "rosetta.config.file";

	@Override
	public URL get() {
		return Optional.ofNullable(System.getProperty(ROSETTA_CONFIG_FILE_PROPERTY))
				.map(this::getUrl)
				.orElse(Thread.currentThread().getContextClassLoader().getResource(FILE_NAME));
	}

	private URL getUrl(String rosettaConfigFile) {
        try {
            return Paths.get(rosettaConfigFile).toUri().toURL();
        } catch (MalformedURLException e) {
			LOGGER.error("System variable path to rosetta config file invalid: {}", rosettaConfigFile, e);
            return null;
        }
    }
}
