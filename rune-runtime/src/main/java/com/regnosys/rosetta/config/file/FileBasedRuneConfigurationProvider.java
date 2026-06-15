package com.regnosys.rosetta.config.file;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.regnosys.rosetta.config.DefaultRuneConfigurationProvider;
import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.config.RuneGeneratorsConfiguration;
import com.regnosys.rosetta.config.RuneModelConfiguration;
import com.regnosys.rosetta.config.RuneSerializationConfiguration;

public class FileBasedRuneConfigurationProvider implements Provider<RuneConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedRuneConfigurationProvider.class);
		
	private final Provider<RuneConfiguration> fallback;
	private final RuneConfigurationFileProvider fileProvider;
	private final ObjectMapper mapper;
	
	@Inject
	public FileBasedRuneConfigurationProvider(DefaultRuneConfigurationProvider fallback, RuneConfigurationFileProvider fileProvider) {
		this.fallback = fallback;
		this.fileProvider = fileProvider;
		this.mapper = new ObjectMapper(new YAMLFactory())
				.addMixIn(RuneConfiguration.class, RuneConfigurationMixin.class)
				.addMixIn(RuneModelConfiguration.class, RuneModelConfigurationMixin.class)
				.addMixIn(RuneGeneratorsConfiguration.class, RuneGeneratorsConfigurationMixin.class)
				.addMixIn(RuneSerializationConfiguration.class, RuneSerializationConfigurationMixin.class);
		mapper.configOverride(RuneGeneratorsConfiguration.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        mapper.configOverride(NamespaceFilter.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
		mapper.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public RuneConfiguration get() {
		RuneConfiguration config = readConfigFromFile();
		if (config != null) {
			return config;
		}
		return fallback.get();
	}
	
	protected RuneConfiguration readConfigFromFile() {
		try {
			URL primaryFile = fileProvider.get();
			if (primaryFile == null) {
				LOGGER.warn("No configuration file was found. Falling back to the default configuration.");
				return null;
			}
			RuneConfiguration primary = mapper.readValue(primaryFile, RuneConfiguration.class);

			// The model, generators and read-only namespaces come from the current project's config only.
			// The serialization config is the union of all configs on the classpath (the current
			// project and its dependencies), with the current project shadowing on id collisions.
			Map<String, RuneSerializationConfiguration> serializationConfigById = new LinkedHashMap<>();
			collectSerializationConfig(primary, serializationConfigById);
			for (URL file : fileProvider.getResources()) {
				if (file.equals(primaryFile)) {
					continue;
				}
				collectSerializationConfig(mapper.readValue(file, RuneConfiguration.class), serializationConfigById);
			}

			return new RuneConfiguration(
					primary.getModel(),
					primary.getDependencies(),
					primary.getGenerators(),
					primary.getReadOnlyNamespaces(),
					new ArrayList<>(serializationConfigById.values()));
		} catch (IOException e) {
      throw new FileBasedRuneConfigurationRuntimeException("Unable to parse the Rosetta configuration.", e);
		}
	}

	private void collectSerializationConfig(RuneConfiguration config, Map<String, RuneSerializationConfiguration> byId) {
		for (RuneSerializationConfiguration serializationConfig : config.getSerializationConfig()) {
			byId.putIfAbsent(serializationConfig.getId(), serializationConfig);
		}
	}
}
