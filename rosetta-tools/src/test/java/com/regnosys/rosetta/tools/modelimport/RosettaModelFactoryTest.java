package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaSegment;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaModelFactoryTest {

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
	void shouldGenerateRosettaEnumeration() {
		XsdSimpleType xsdSimpleType = getXsdElements("src/test/resources/model-import/enum.xsd", XsdSimpleType.class).get(0);
		
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaEnumeration rosettaEnum = factory.createRosettaEnumeration(xsdSimpleType);
		
		// assert
		assertEquals("FooEnum", rosettaEnum.getName());
		assertEquals("FooEnum definition.", rosettaEnum.getDefinition());
		
		List<RosettaEnumValue> enumValues = rosettaEnum.getEnumValues();
		assertEquals(3, enumValues.size());
		
		RosettaEnumValue value1 = enumValues.get(0);
		assertEquals("Value1", value1.getName());
		assertEquals("Value1 name", value1.getDisplay());
		assertEquals("Value1 definition.", value1.getDefinition());
		assertEquals(rosettaEnum, value1.getEnumeration());
		
		RosettaEnumValue value2 = enumValues.get(1);
		assertEquals("Value2", value2.getName());
		assertEquals("Value2 name", value2.getDisplay());
		assertEquals("Value2 definition.", value2.getDefinition());
		assertEquals(rosettaEnum, value2.getEnumeration());
		
		RosettaEnumValue value3 = enumValues.get(2);
		assertEquals("Value3", value3.getName());
		assertEquals("Value3 name", value3.getDisplay());
		assertEquals("Value3 definition.", value3.getDefinition());
		assertEquals(rosettaEnum, value3.getEnumeration());
	}
	
	
	
	@Test
	void shouldGenerateRosettaBody() {
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaBody rosettaBody = factory.createBody(BODY_TYPE, BODY_NAME, BODY_DEFINITION);
		
		// assert
		assertEquals(BODY_TYPE, rosettaBody.getBodyType());
		assertEquals(BODY_NAME, rosettaBody.getName());
		assertEquals(BODY_DEFINITION, rosettaBody.getDefinition());
	}
	
	@Test
	void shouldGenerateRosettaSegment() {
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaSegment rosettaSegment = factory.createSegment(SEGMENT);
		
		// assert
		assertEquals(SEGMENT, rosettaSegment.getName());
		assertNull(rosettaSegment.getModel());
	}
	
	@Test
	void shouldGenerateRosettaCorpus() {
		// test
		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaBody rosettaBody = factory.createBody(BODY_TYPE, BODY_NAME, BODY_DEFINITION);
		RosettaCorpus rosettaCorpus = factory.createCorpus(rosettaBody, CORPUS_TYPE, CORPUS_NAME, CORPUS_DISPLAY_NAME, CORPUS_DEFINITION);
		
		// assert
		assertEquals(CORPUS_TYPE, rosettaCorpus.getCorpusType());
		assertEquals(CORPUS_NAME, rosettaCorpus.getName());
		assertEquals(CORPUS_DISPLAY_NAME, rosettaCorpus.getDisplayName());
		assertEquals(CORPUS_DEFINITION, rosettaCorpus.getDefinition());
	}
	
	@Test
	void shouldGenerateAddAttributesToData() {
		GenerationProperties properties = mock(GenerationProperties.class);
		when(properties.getNamespace()).thenReturn(NAMESPACE);
		when(properties.getNamespaceDefinition()).thenReturn(NAMESPACE_DEFINITION);

		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaBody rosettaBody = factory.createBody(BODY_TYPE, BODY_NAME, BODY_DEFINITION);
		RosettaCorpus rosettaCorpus = factory.createCorpus(rosettaBody, CORPUS_TYPE, CORPUS_NAME, CORPUS_DISPLAY_NAME, CORPUS_DEFINITION);
		RosettaSegment rosettaSegment = factory.createSegment(SEGMENT);
		RosettaModel rosettaModel = factory.createRosettaModel("type", properties, List.of());
		
		// Load xsd elements, create data elements, and add to rosetta model elements
		List<XsdNamedElements> xsdElements = getXsdElements(DATA_XSD_PATH, XsdNamedElements.class);
		List<Data> dataTypes = xsdElements.stream()
			.map(element -> factory.createData(element))
			.collect(Collectors.toList());
		rosettaModel.getElements().addAll(dataTypes);
		
		// test
		XsdComplexType xsdComplexTypeFoo = (XsdComplexType) xsdElements.get(3);
		factory.addAttributesToData(xsdComplexTypeFoo, rosettaBody, rosettaCorpus, rosettaSegment);
		
		// assert
		Data foo = dataTypes.get(3);
		
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
	void shouldGenerateAddAttributesToData2() {
		GenerationProperties properties = mock(GenerationProperties.class);
		when(properties.getNamespace()).thenReturn(NAMESPACE);
		when(properties.getNamespaceDefinition()).thenReturn(NAMESPACE_DEFINITION);

		RosettaModelFactory factory = new RosettaModelFactory(resourceSet, rosettaTypeMappings);
		RosettaBody rosettaBody = factory.createBody(BODY_TYPE, BODY_NAME, BODY_DEFINITION);
		RosettaCorpus rosettaCorpus = factory.createCorpus(rosettaBody, CORPUS_TYPE, CORPUS_NAME, CORPUS_DISPLAY_NAME, CORPUS_DEFINITION);
		RosettaSegment rosettaSegment = factory.createSegment(SEGMENT);
		RosettaModel rosettaModel = factory.createRosettaModel("type", properties, List.of());
		
		// Load xsd elements, create data elements, and add to rosetta model elements
		List<XsdNamedElements> xsdElements = getXsdElements(DATA2_XSD_PATH, XsdNamedElements.class);
		List<Data> dataTypes = xsdElements.stream()
			.map(element -> factory.createData(element))
			.collect(Collectors.toList());
		rosettaModel.getElements().addAll(dataTypes);
		
		// test
		XsdComplexType xsdComplexTypeFoo = (XsdComplexType) xsdElements.get(3);
		factory.addAttributesToData(xsdComplexTypeFoo, rosettaBody, rosettaCorpus, rosettaSegment);
		
		// assert
		Data foo = dataTypes.get(3);
		
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
	
	private <T extends XsdAbstractElement> List<T> getXsdElements(String xsdPath, Class<T> clazz) {
		XsdParser xsdParser = new XsdParser(xsdPath);
		return xsdParser.getResultXsdSchemas()
                .map(XsdSchema::getElements)
                .flatMap(Collection::stream)
                .map(ReferenceBase::getElement)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
	}
}
