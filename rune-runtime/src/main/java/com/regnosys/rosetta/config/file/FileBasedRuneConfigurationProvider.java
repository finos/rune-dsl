package com.regnosys.rosetta.config.file;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.config.DefaultRuneConfigurationProvider;
import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.config.RuneConfigurationService;
import com.regnosys.rosetta.config.RuneNamespaceConfiguration;

public class FileBasedRuneConfigurationProvider implements Provider<RuneConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedRuneConfigurationProvider.class);

	private final Provider<RuneConfiguration> fallback;
	private final RuneConfigurationFileProvider fileProvider;
	private final RuneConfigurationService configurationService;

	@Inject
	public FileBasedRuneConfigurationProvider(DefaultRuneConfigurationProvider fallback, RuneConfigurationFileProvider fileProvider) {
		this.fallback = fallback;
		this.fileProvider = fileProvider;
		this.configurationService = new RuneConfigurationService();
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
			RuneConfiguration primary = configurationService.read(primaryFile);

			// The model and generators come from the current project's config only.
			// The namespace config is the union of all configs on the classpath (the current
			// project and its dependencies), with the current project shadowing on id collisions.
			List<RuneNamespaceConfiguration> mergedNamespaceConfig = new ArrayList<>();
			Set<String> seenIds = new HashSet<>();
			collectNamespaceConfig(primary, mergedNamespaceConfig, seenIds);
			for (URL file : fileProvider.getResources()) {
				if (file.equals(primaryFile)) {
					continue;
				}
				collectNamespaceConfig(configurationService.read(file), mergedNamespaceConfig, seenIds);
			}

			return new RuneConfiguration(
					primary.getModel(),
					primary.getDependencies(),
					primary.getGenerators(),
					mergedNamespaceConfig);
		} catch (IOException e) {
			throw new FileBasedRuneConfigurationRuntimeException("Unable to parse the Rosetta configuration.", e);
		}
	}

	private void collectNamespaceConfig(RuneConfiguration config, List<RuneNamespaceConfiguration> merged, Set<String> seenIds) {
		for (RuneNamespaceConfiguration namespaceConfig : config.getNamespaceConfig()) {
			// Entries with an id are unioned (deduped) by that id, current project first. Entries with
			// no id (e.g. a plain read-only namespace) are not deduplicated and are all kept.
			if (namespaceConfig.getId() == null || seenIds.add(namespaceConfig.getId())) {
				merged.add(namespaceConfig);
			}
		}
	}
}
