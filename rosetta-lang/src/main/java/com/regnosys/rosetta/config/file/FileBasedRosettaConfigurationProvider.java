package com.regnosys.rosetta.config.file;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.regnosys.rosetta.config.DefaultRosettaConfigurationProvider;
import com.regnosys.rosetta.config.RosettaConfiguration;
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration;
import com.regnosys.rosetta.config.RosettaModelConfiguration;

public class FileBasedRosettaConfigurationProvider implements Provider<RosettaConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedRosettaConfigurationProvider.class);
	
	private static final String FILE_NAME = "rosetta-config.yml";
	
	private final Provider<RosettaConfiguration> fallback;
	private final ObjectMapper mapper;
	
	@Inject
	public FileBasedRosettaConfigurationProvider(DefaultRosettaConfigurationProvider fallback) {
		this.fallback = fallback;
		this.mapper = new ObjectMapper(new YAMLFactory())
				.addMixIn(RosettaConfiguration.class, RosettaConfigurationMixin.class)
				.addMixIn(RosettaModelConfiguration.class, RosettaModelConfigurationMixin.class)
				.addMixIn(RosettaGeneratorsConfiguration.class, RosettaGeneratorsConfigurationMixin.class);
		mapper.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
	}

	@Override
	public RosettaConfiguration get() {
		RosettaConfiguration config = readConfigFromFile();
		if (config != null) {
			return config;
		}
		return fallback.get();
	}
	
	protected RosettaConfiguration readConfigFromFile() {
		try {
			URL file = Thread.currentThread().getContextClassLoader().getResource(FILE_NAME);
			if (file != null) {
				return mapper.readValue(file, RosettaConfiguration.class);
			}
			LOGGER.info("No file named " + FILE_NAME + " was found on the classpath. Falling back to the default configuration.");
			return null;
		} catch (IOException e) {
			LOGGER.error("Could not read Rosetta configuration.", e);
			return null;
		}
	}
}
