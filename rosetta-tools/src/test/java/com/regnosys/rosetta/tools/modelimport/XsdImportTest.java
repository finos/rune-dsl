package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xmlet.xsdparser.core.XsdParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.rosetta.util.serialisation.RosettaXMLConfiguration;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class XsdImportTest {

	private static final String NAMESPACE = "test.ns";
	private static final String NAMESPACE_DEFINITION = "test.ns definition";
	
	private static final String SYN_SOURCE_NAME = "TEST_SYN_SOURCE";
	
	
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
	
	private void runTest(String xsdName) throws IOException, URISyntaxException {
		Path baseFolder = Path.of(getClass().getResource("/model-import/" + xsdName).toURI());
		Path xsdFile = baseFolder.resolve("schema.xsd");
		Path expectedFolder = baseFolder.resolve("expected");
		
		GenerationProperties properties = mockProperties();

		// Load xsd elements
		XsdParser parsedInstance = new XsdParser(xsdFile.toString());
				
		// Test rosetta
		ResourceSet set = xsdImport.generateRosetta(parsedInstance, properties);
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
		RosettaXMLConfiguration xmlConfig = xsdImport.generateXMLConfiguration(parsedInstance, properties);
		
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

	private GenerationProperties mockProperties() {
		GenerationProperties properties = mock(GenerationProperties.class);
		when(properties.getNamespace()).thenReturn(NAMESPACE);
		when(properties.getNamespaceDefinition()).thenReturn(NAMESPACE_DEFINITION);
		when(properties.getSynonymSourceName()).thenReturn(SYN_SOURCE_NAME);
		return properties;
	}
	
	private List<Path> getResourceFiles(Path expectedPath) throws IOException {
		return Files.list(expectedPath).filter(r -> r.getFileName().toString().endsWith(".rosetta")).toList();
	}
}
