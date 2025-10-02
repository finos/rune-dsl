package com.regnosys.rosetta.config.file;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.regnosys.rosetta.config.DefaultRosettaConfigurationProvider;
import com.regnosys.rosetta.config.RosettaConfiguration;
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration;
import com.regnosys.rosetta.config.RosettaModelConfiguration;

public class FileBasedRosettaConfigurationProvider implements Provider<RosettaConfiguration>, javax.inject.Provider<RosettaConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedRosettaConfigurationProvider.class);
		
	private final Provider<RosettaConfiguration> fallback;
	private final Provider<URL> fileProvider;
	private final ObjectMapper mapper;
	
	@Inject
	public FileBasedRosettaConfigurationProvider(DefaultRosettaConfigurationProvider fallback, RosettaConfigurationFileProvider fileProvider) {
		this.fallback = fallback;
		this.fileProvider = fileProvider;
		this.mapper = new ObjectMapper(new YAMLFactory())
				.addMixIn(RosettaConfiguration.class, RosettaConfigurationMixin.class)
				.addMixIn(RosettaModelConfiguration.class, RosettaModelConfigurationMixin.class)
				.addMixIn(RosettaGeneratorsConfiguration.class, RosettaGeneratorsConfigurationMixin.class);
		mapper.configOverride(RosettaGeneratorsConfiguration.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        mapper.configOverride(NamespaceFilter.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
		mapper.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
			URL file = fileProvider.get();
			if (file != null) {
				return mapper.readValue(file, RosettaConfiguration.class);
			}
			LOGGER.debug("No configuration file was found. Falling back to the default configuration.");
			return null;
		} catch (IOException e) {
			LOGGER.error("Could not read Rosetta configuration.", e);
			return null;
		}
	}
}
