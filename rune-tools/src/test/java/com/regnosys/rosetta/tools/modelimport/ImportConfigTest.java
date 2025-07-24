package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ImportConfigTest {
	@Test
	void shouldLoadConfigFromFile() throws FileNotFoundException, IOException {
		// test
		ImportConfig config = getConfig("src/test/resources/model-import/test-config.yml");
		
		// assert
		assertEquals("test.ns", config.getTarget().getNamespace());
		assertEquals("Test namespace definition", config.getTarget().getNamespaceDefinition());
		assertNotNull(config.getTarget().getNameOverrides());
		assertNotNull(config.getTarget().getPreferences());
	}

	private ImportConfig getConfig(String path) throws FileNotFoundException, IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(new File(path), ImportConfig.class);
	}
}
