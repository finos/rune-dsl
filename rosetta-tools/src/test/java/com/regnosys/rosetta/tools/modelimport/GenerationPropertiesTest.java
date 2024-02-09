/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
