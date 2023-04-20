package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class GenerationPropertiesTest {
	
	
	@Test
	void shouldLoadPropertiesFromFile() throws FileNotFoundException, IOException {
		Properties properties = getProperties("src/test/resources/model-import/test.properties");
    	
		// test
		GenerationProperties generationProperties = new GenerationProperties(properties);
		
		// assert
		assertEquals("test.ns", generationProperties.getNamespace());
		assertEquals("Test namespace definition", generationProperties.getNamespaceDefinition());
		assertEquals("TestBodyType", generationProperties.getBodyType());
		assertEquals("TestBodyName", generationProperties.getBodyName());
		assertEquals("Test body definition", generationProperties.getBodyDefinition());
		assertEquals("TestCorpusType", generationProperties.getCorpusType());
		assertEquals("TestCorpusName", generationProperties.getCorpusName());
		assertEquals("Test corpus display name", generationProperties.getCorpusDisplayName());
		assertEquals("Test corpus definition", generationProperties.getCorpusDefinition());
		assertEquals("xsdName", generationProperties.getSegmentName());
		assertEquals("TEST_SYN_SOURCE", generationProperties.getSynonymSourceName());
	}

	private Properties getProperties(String path) throws FileNotFoundException, IOException {
		try (InputStream input = new FileInputStream(path)) {
        	Properties properties = new Properties();
        	properties.load(input);
        	return properties;
        }
	}
}
