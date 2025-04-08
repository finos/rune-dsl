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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xmlet.xsdparser.core.XsdParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.rosetta.util.serialisation.RosettaXMLConfiguration;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class XsdImportTest {

	private static final String NAMESPACE = "test.ns";
	private static final String NAMESPACE_DEFINITION = "test.ns definition";
	
	
	@Inject
	Provider<ResourceSet> resourceSetProvider;
	@Inject
	ValidationTestHelper validationTestHelper;
	
	@Inject
	RosettaBuiltinsService builtinResources;
	@Inject
	RBuiltinTypeService builtins;
	@Inject
	XsdUtil util;

	@Inject
	XsdImport xsdImport;
	
	private ResourceSet resourceSet;
	private RosettaXsdMapping rosettaXsdMapping;
	@BeforeEach
	void beforeEach() {
		rosettaXsdMapping = new RosettaXsdMapping(builtins, util);
		resourceSet = resourceSetProvider.get();
		// Add builtin types to the resource set
		new RosettaModelFactory(resourceSet, builtinResources);
		rosettaXsdMapping.initializeBuiltins(resourceSet);
	}
	
	private void assertNoUnresolvedXsdElements(XsdParser parsedInstance) {
		// assertEquals("", parsedInstance.getUnsolvedReferences().stream().map(r -> r.getUnsolvedReference().getRef()).collect(Collectors.joining("\n")));
	}
	
	private void runTest(String xsdName) throws IOException, URISyntaxException {
		Path baseFolder = Path.of(getClass().getResource("/model-import/" + xsdName).toURI());
		Path expectedFolder = baseFolder.resolve("expected");
		Path configFile = baseFolder.resolve(xsdName + "-config.yml");
		
		ImportConfig config;
		if (Files.exists(configFile)) {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			try (InputStream input = Files.newInputStream(configFile)) {
				config = mapper.readValue(input, ImportConfig.class);
	        }
		} else {
			config = mockConfig(baseFolder);
		}
		Path xsdFile = baseFolder.resolve(config.getSchemaLocation()).normalize();

		// Load xsd elements
		RosettaXsdParser parsedInstance = new RosettaXsdParser(xsdFile.toString());
		assertNoUnresolvedXsdElements(parsedInstance);
				
		// Test rosetta
		ResourceSet set = xsdImport.generateRosetta(parsedInstance, config.getTarget());
		List<String> resourceNames = set.getResources().stream()
				.map(Resource::getURI)
				.filter(uri -> uri.scheme() == null)
				.map(Object::toString)
				.toList();
		
		List<Path> expectedResources = getResourceFiles(expectedFolder);
		
		assertEquals(
			expectedResources.stream().map(r -> r.getFileName().toString()).collect(Collectors.toSet()),
			new HashSet<>(resourceNames)
		);
		
		for (Path resource: expectedResources) {
			String expected = Files.readString(resource);
			
			Resource actualResource = set.getResource(URI.createURI(resource.getFileName().toString()), false);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			actualResource.save(output, null);
			String actual = output.toString(StandardCharsets.UTF_8);
			
			assertEquals(expected, actual);
		}
		
		set.getResources().forEach(resource -> validationTestHelper.assertNoIssues(resource));
	
		// Test XML config
		RosettaXMLConfiguration xmlConfig = xsdImport.generateXMLConfiguration(parsedInstance, config.getTarget());
		
		String expected = Files.readString(expectedFolder.resolve("xml-config.json"));
		ObjectMapper mapper = XsdImportMain.getObjectMapper();
		String actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlConfig);
		assertEquals(expected, actual);
		
		// Test deserialisation
		assertEquals(xmlConfig, mapper.readValue(actual, RosettaXMLConfiguration.class));
	}
	
	@Test
	void testEnum() throws IOException, URISyntaxException {
		runTest("enum");
	}
	
	@Test
	void testData() throws IOException, URISyntaxException {
		runTest("data");
	}

	@Test
	void testData2() throws IOException, URISyntaxException {
		runTest("data2");
	}
	
	@Test
	void testChoice() throws IOException, URISyntaxException {
		runTest("choice");
	}
	
	@Test
	void testDataAndEnum() throws IOException, URISyntaxException {
		runTest("data-and-enum");
	}
	
	@Test
	void testSimpleTypeExtension() throws IOException, URISyntaxException {
		runTest("simple-type-extension");
	}
	
	@Test
	void testTopLevel() throws IOException, URISyntaxException {
		runTest("top-level");
	}
	
	@Test
	void testMulti() throws IOException, URISyntaxException {
		runTest("multi");
	}

    @Test
    void testNestedData() throws IOException, URISyntaxException {
        runTest("nested-data");
    }
    
    @Test
    void testSubstitution() throws IOException, URISyntaxException {
        runTest("substitution");
    }

	private ImportConfig mockConfig(Path basePath) {
		ImportConfig config = new ImportConfig(basePath.resolve("schema.xsd").toString(), new ImportTargetConfig(NAMESPACE, NAMESPACE_DEFINITION, Collections.emptyMap(), null));
		return config;
	}
	
	private List<Path> getResourceFiles(Path expectedPath) throws IOException {
		return Files.list(expectedPath).filter(r -> r.getFileName().toString().endsWith(".rosetta")).toList();
	}
}
