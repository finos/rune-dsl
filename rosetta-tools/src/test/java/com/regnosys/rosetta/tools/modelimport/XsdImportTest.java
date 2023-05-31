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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class XsdImportTest {

	private static final String NAMESPACE = "test.ns";
	private static final String NAMESPACE_DEFINITION = "test.ns definition";
	
	private static final String BODY_TYPE = "TestBodyType";
	private static final String BODY_NAME = "TestBodyName";
	private static final String BODY_DEFINITION = "Test body definition.";
	
	private static final String SEGMENT = "TestSegment";
	
	private static final String CORPUS_TYPE = "TestCorpusType";
	private static final String CORPUS_NAME = "TestCorpusName";
	private static final String CORPUS_DISPLAY_NAME = "Test corpus display name.";
	private static final String CORPUS_DEFINITION = "Test corpus definition.";
	
	private static final String SYN_SOURCE_NAME = "TEST_SYN_SOURCE";
	
	
	@Inject
	Provider<ResourceSet> resourceSetProvider;
	
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
	private RosettaModelFactory modelFactory;
	@BeforeEach
	void beforeEach() {
		rosettaXsdMapping = new RosettaXsdMapping(builtins, util);
		resourceSet = resourceSetProvider.get();
		modelFactory = new RosettaModelFactory(resourceSet, builtinResources);
		rosettaXsdMapping.initializeBuiltinTypeMap(resourceSet);
	}
	
	private void runTest(String xsdName) throws IOException {
		String xsdFile = "src/test/resources/model-import/" + xsdName + ".xsd";
		String expectedFolder = "model-import/" + xsdName + "-result";
		
		GenerationProperties properties = mockProperties();

		// Load xsd elements
		List<XsdAbstractElement> xsdElements = getXsdElements(xsdFile);
				
		// test
		ResourceSet set = xsdImport.generateRosetta(xsdElements, properties, List.of());
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
			String actual = new String(output.toByteArray(), StandardCharsets.UTF_8);
			
			assertEquals(expected, actual);
		}
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
	void testDataAndEnum() throws IOException {
		runTest("data-and-enum");
	}
	

	private GenerationProperties mockProperties() {
		GenerationProperties properties = mock(GenerationProperties.class);
		when(properties.getNamespace()).thenReturn(NAMESPACE);
		when(properties.getNamespaceDefinition()).thenReturn(NAMESPACE_DEFINITION);
		when(properties.getBodyName()).thenReturn(BODY_NAME);
		when(properties.getBodyType()).thenReturn(BODY_TYPE);
		when(properties.getBodyDefinition()).thenReturn(BODY_DEFINITION);
		when(properties.getCorpusName()).thenReturn(CORPUS_NAME);
		when(properties.getCorpusType()).thenReturn(CORPUS_TYPE);
		when(properties.getCorpusDisplayName()).thenReturn(CORPUS_DISPLAY_NAME);
		when(properties.getCorpusDefinition()).thenReturn(CORPUS_DEFINITION);
		when(properties.getSegmentName()).thenReturn(SEGMENT);
		when(properties.getSynonymSourceName()).thenReturn(SYN_SOURCE_NAME);
		return properties;
	}
	
	private List<XsdAbstractElement> getXsdElements(String xsdPath) {
		XsdParser xsdParser = new XsdParser(xsdPath);
		return xsdParser.getResultXsdSchemas()
                .map(XsdSchema::getElements)
                .flatMap(Collection::stream)
                .map(ReferenceBase::getElement)
                .collect(Collectors.toList());
	}
	
	private List<String> getResourceFiles(String path) {
	    List<String> filenames = new ArrayList<>();

	    try (
	            InputStream in = getResourceAsStream(path);
	            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
	        String resource;

	        while ((resource = br.readLine()) != null) {
	            filenames.add(resource);
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
