package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.xmlet.xsdparser.xsdelements.XsdSchema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
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
	
	private void runTest(String xsdName) throws IOException {
		String xsdFile = "src/test/resources/model-import/" + xsdName + ".xsd";
		String expectedFolder = "model-import/" + xsdName + "-result";
		
		GenerationProperties properties = mockProperties();

		// Load xsd elements
		XsdSchema schema = getXsdSchema(xsdFile);
				
		// Test rosetta
		ResourceSet set = xsdImport.generateRosetta(schema, properties);
		List<String> resourceNames = set.getResources().stream()
				.map(r -> r.getURI())
				.filter(uri -> uri.scheme() == null)
				.map(uri -> uri.toString())
				.collect(Collectors.toList());
		
		List<String> expectedResources = getResourceFiles(expectedFolder);
		
		assertEquals(new HashSet<>(expectedResources), new HashSet<>(resourceNames));
		
		for (String resource: expectedResources) {
			String expected = Resources.toString(Resources.getResource(expectedFolder + "/" + resource), StandardCharsets.UTF_8);
			
			Resource actualResource = set.getResource(URI.createURI(resource), false);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			actualResource.save(output, null);
			String actual = new String(output.toByteArray(), StandardCharsets.UTF_8).replaceAll("\r\n", "\n");
			
			assertEquals(expected, actual);
		}
		
		set.getResources().forEach(resource -> validationTestHelper.assertNoIssues(resource));
	
		// Test XML config
		RosettaXMLConfiguration xmlConfig = xsdImport.generateXMLConfiguration(schema, properties);
		
		String expected = Resources.toString(Resources.getResource(expectedFolder + "/xml-config.json"), StandardCharsets.UTF_8);
		ObjectMapper mapper = XsdImportMain.getObjectMapper();
		String actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlConfig).replaceAll("\r\n", "\n");
		assertEquals(expected, actual);
		
		// Test deserialisation
		assertEquals(xmlConfig, mapper.readValue(actual, RosettaXMLConfiguration.class));
	}
	
	@Test
	void testEnum() throws IOException {
		runTest("enum");
	}
	
	@Test
	void testData() throws IOException {
		runTest("data");
	}

	@Test
	void testData2() throws IOException {
		runTest("data2");
	}
	
	@Test
	void testChoice() throws IOException {
		runTest("choice");
	}
	
	@Test
	void testDataAndEnum() throws IOException {
		runTest("data-and-enum");
	}
	
	@Test
	void testSimpleTypeExtension() throws IOException {
		runTest("simple-type-extension");
	}
	
	@Test
	void testTopLevel() throws IOException {
		runTest("top-level");
	}

	private GenerationProperties mockProperties() {
		GenerationProperties properties = mock(GenerationProperties.class);
		when(properties.getNamespace()).thenReturn(NAMESPACE);
		when(properties.getNamespaceDefinition()).thenReturn(NAMESPACE_DEFINITION);
		when(properties.getSynonymSourceName()).thenReturn(SYN_SOURCE_NAME);
		return properties;
	}
	
	private XsdSchema getXsdSchema(String xsdPath) {
		XsdParser xsdParser = new XsdParser(xsdPath);
		return xsdParser.getResultXsdSchemas()
				.findAny()
				.orElseThrow();
	}
	
	private List<String> getResourceFiles(String path) {
	    List<String> filenames = new ArrayList<>();

	    try (
	            InputStream in = getResourceAsStream(path);
	            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
	        String resource;

	        while ((resource = br.readLine()) != null) {
	        	if (resource.endsWith(".rosetta")) {
	        		filenames.add(resource);
	        	}
	        }
	    } catch (IOException e) {
			throw new RuntimeException(e);
		}

	    return filenames;
	}

	private InputStream getResourceAsStream(String resource) {
	    final InputStream in
	            = getContextClassLoader().getResourceAsStream(resource);

	    return in == null ? getClass().getResourceAsStream(resource) : in;
	}

	private ClassLoader getContextClassLoader() {
	    return Thread.currentThread().getContextClassLoader();
	}
}
