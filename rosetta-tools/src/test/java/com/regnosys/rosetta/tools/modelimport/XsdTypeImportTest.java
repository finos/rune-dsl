package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class XsdTypeImportTest {

	// contains documentation elements with source attributes
	private static final String DATA_XSD_PATH = "src/test/resources/model-import/data.xsd";
	// contains documentation elements without source attributes
	private static final String DATA2_XSD_PATH = "src/test/resources/model-import/data2.xsd";

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
	
	
	@Inject
	ResourceSet resourceSet;
	
	@Inject
	RosettaTypeMappings rosettaTypeMappings;
	
	@Test
	void shouldGenerateDataWithDocumentation() {
		GenerationProperties properties = mockProperties();

		// Load xsd elements, create data elements, and add to rosetta model elements
		List<XsdAbstractElement> xsdElements = getXsdElements(DATA_XSD_PATH);
		
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		XsdTypeImport xsdTypeImport = new XsdTypeImport(factory);
		RosettaModel model = xsdTypeImport.generateTypes(xsdElements, properties, List.of());
		
		// assert
		EList<RosettaRootElement> rosettaElements = model.getElements();
		assertEquals(7, rosettaElements.size());
		
		Data foo = (Data) rosettaElements.get(6);
		
		assertEquals("Foo", foo.getName());
		assertEquals("Foo definition.", foo.getDefinition());
		
		List<Attribute> attrs = foo.getAttributes();
		assertEquals(8, attrs.size());
		
		assertAttribute(attrs.get(0), "fooBooleanAttr", "boolean", 1, 1, false, "FooBooleanAttr definition.");
		assertAttribute(attrs.get(1), "fooStrAttr", "string", 1, 1, false, "FooStrAttr definition.");
		assertAttribute(attrs.get(2), "fooDecimalAttr", "number", 0, 1, false, "FooDecimalAttr definition.");
		assertAttribute(attrs.get(3), "fooStringWithRestrictionAttr", "string", 1, 1, false, "FooStringWithRestrictionAttr definition.", 
				"Specifies a character string with a maximum length of 500 characters.", "Max500Text");
		assertAttribute(attrs.get(4), "fooDecimalWithRestrictionAttr", "number", 0, 1, false, "FooDecimalWithRestrictionAttr definition.", 
				"Number (max 999) of objects represented as an integer.", "Max3Number");
		assertAttribute(attrs.get(5), "fooBarAttr", "Bar", 1, 1, false, "FooBarAttr definition.");
		assertAttribute(attrs.get(6), "fooStrListAttr", "string", 0, 0, true, "FooStrListAttr definition.");
		assertAttribute(attrs.get(7), "fooBarListAttr", "Bar", 1, 2, false, "FooBarListAttr definition.");
	}

	@Test
	void shouldGenerateDataWithDocumentationWithoutSourceAttr() {
		GenerationProperties properties = mockProperties();

		// Load xsd elements, create data elements, and add to rosetta model elements
		List<XsdAbstractElement> xsdElements = getXsdElements(DATA2_XSD_PATH);
		
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		XsdTypeImport xsdTypeImport = new XsdTypeImport(factory);
		RosettaModel model = xsdTypeImport.generateTypes(xsdElements, properties, List.of());
		
		// assert
		EList<RosettaRootElement> rosettaElements = model.getElements();
		assertEquals(7, rosettaElements.size());
		
		Data foo = (Data) rosettaElements.get(6);
		assertEquals("Foo", foo.getName());
		assertEquals("Foo definition.", foo.getDefinition());
		
		List<Attribute> attrs = foo.getAttributes();
		assertEquals(8, attrs.size());
		
		assertAttribute(attrs.get(0), "fooBooleanAttr", "boolean", 1, 1, false, "FooBooleanAttr definition.");
		assertAttribute(attrs.get(1), "fooStrAttr", "string", 1, 1, false, "FooStrAttr definition.");
		assertAttribute(attrs.get(2), "fooDecimalAttr", "number", 0, 1, false, "FooDecimalAttr definition.");
		assertAttribute(attrs.get(3), "fooStringWithRestrictionAttr", "string", 1, 1, false, "FooStringWithRestrictionAttr definition.", 
				"Specifies a character string with a maximum length of 500 characters.", "Max500Text");
		assertAttribute(attrs.get(4), "fooDecimalWithRestrictionAttr", "number", 0, 1, false, "FooDecimalWithRestrictionAttr definition.", 
				"Number (max 999) of objects represented as an integer.", "Max3Number");
		assertAttribute(attrs.get(5), "fooBarAttr", "Bar", 1, 1, false, "FooBarAttr definition.");
		assertAttribute(attrs.get(6), "fooStrListAttr", "string", 0, 0, true, "FooStrListAttr definition.");
		assertAttribute(attrs.get(7), "fooBarListAttr", "Bar", 1, 2, false, "FooBarListAttr definition.");
	}

	private void assertAttribute(Attribute attr, String name, String type, int inf, int sup, boolean unbounded, String definition) {
		assertEquals(name, attr.getName());
		assertEquals(type, attr.getTypeCall().getType().getName());
		
		RosettaCardinality card = attr.getCard();
		assertEquals(inf, card.getInf());
		assertEquals(sup, card.getSup());
		assertEquals(unbounded, card.isUnbounded());
		
		assertEquals(definition, attr.getDefinition());
	}
	
	private void assertAttribute(Attribute attr, String name, String type, int inf, int sup, boolean unbounded, String definition, String provision, String segmentText) {
		assertAttribute(attr, name, type, inf, sup, unbounded, definition);
		
		RosettaDocReference docReference = attr.getReferences().get(0);
		assertEquals(provision, docReference.getProvision());
		
		RegulatoryDocumentReference regReference = docReference.getDocReference();
		assertEquals(BODY_NAME, regReference.getBody().getName());
		assertEquals(CORPUS_NAME, regReference.getCorpuses().get(0).getName());
		
		RosettaSegmentRef rosettaSegmentRef = regReference.getSegments().get(0);
		assertEquals(SEGMENT, rosettaSegmentRef.getSegment().getName());
		assertEquals(segmentText, rosettaSegmentRef.getSegmentRef());
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
}
