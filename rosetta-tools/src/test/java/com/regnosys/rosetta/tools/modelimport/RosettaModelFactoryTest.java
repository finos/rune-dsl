package com.regnosys.rosetta.tools.modelimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaSegment;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaModelFactoryTest {

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
