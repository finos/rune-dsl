package com.regnosys.rosetta.tools.modelimport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAttribute;
import org.xmlet.xsdparser.xsdelements.XsdChoice;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdExtension;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdSimpleContent;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;
import org.xmlet.xsdparser.xsdelements.enums.UsageEnum;
import org.xmlet.xsdparser.xsdelements.visitors.AttributesVisitor;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.rosetta.util.serialisation.AttributeXMLConfiguration;
import com.rosetta.util.serialisation.AttributeXMLRepresentation;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

public class XsdTypeImport extends AbstractXsdImport<XsdComplexType, Data> {
	public final String UNBOUNDED = "unbounded";
	public final String SIMPLE_EXTENSION_ATTRIBUTE_NAME = "value";

	private final XsdUtil util;
	private final RosettaBuiltinsService builtins;
	
	@Inject
	public XsdTypeImport(XsdUtil util, RosettaBuiltinsService builtins) {
		super(XsdComplexType.class);
		this.util = util;
		this.builtins = builtins;
	}
	
	private boolean isChoice(XsdComplexType xsdType) {
		return xsdType.getXsdChildElement() instanceof XsdChoice;
	}
	private Stream<XsdElement> getTypedXsdElements(XsdComplexType xsdType) {
		return Optional.of(xsdType)
				.map(XsdComplexType::getElements).stream()
				.flatMap(List::stream)
				.map(ReferenceBase::getElement)
				.filter(XsdElement.class::isInstance)
				.map(XsdElement.class::cast)
				.filter(xsdElement -> xsdElement.getType() != null);
	}
	private Stream<XsdAttribute> getTypedXsdAttributes(XsdComplexType xsdType) {
		return Optional.of(xsdType)
				.map(XsdComplexType::getSimpleContent)
				.map(XsdSimpleContent::getXsdExtension)
				.map(XsdExtension::getVisitor)
				.filter(v -> v instanceof AttributesVisitor)
				.map(v -> (AttributesVisitor)v)
				.map(AttributesVisitor::getAllAttributes).stream()
				.flatMap(List::stream)
				.filter(xsdElement -> xsdElement.getType() != null);
	}
	private Optional<XsdSimpleType> getBaseSimpleType(XsdComplexType xsdType) {
		return Optional.of(xsdType)
				.map(XsdComplexType::getSimpleContent)
				.map(XsdSimpleContent::getXsdExtension)
				.map(XsdExtension::getBaseAsSimpleType);
	}

	@Override
	public Data registerType(XsdComplexType xsdType, RosettaXsdMapping xsdMapping, Map<XsdNamedElements, String> rootTypeNames, GenerationProperties properties) {
		Data data = SimpleFactory.eINSTANCE.createData();
		data.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(data::setDefinition);
		xsdMapping.registerComplexType(xsdType, data);
		
		boolean isChoice = isChoice(xsdType);
		
		// If the complex type extends a simple type, simulate this
		// by adding a `value` attribute of the corresponding type.
		if (getBaseSimpleType(xsdType).isPresent()) {
			data.getAttributes().add(
				registerValueAttribute(xsdType, xsdMapping)
			);
		}
		
		// Map XSD elements to Rosetta attributes.
		getTypedXsdElements(xsdType)
			.map(element -> registerAttribute(element, isChoice, xsdMapping))
			.forEach(data.getAttributes()::add);
		
		// Map XSD attributes to Rosetta attributes.
		getTypedXsdAttributes(xsdType)
			.map(attribute -> registerAttribute(attribute, xsdMapping))
			.forEach(data.getAttributes()::add);
		
		// Add a one-of condition if it is a `xs:choice` type.
		if (isChoice) {
			Condition choice = SimpleFactory.eINSTANCE.createCondition();
			choice.setName("Choice");
			
			OneOfOperation oneOf = ExpressionFactory.eINSTANCE.createOneOfOperation();
			oneOf.setOperator("one-of");
			choice.setExpression(oneOf);
			
			data.getConditions().add(choice);
		}
		
		return data;
	}

	@Override
	public void completeType(XsdComplexType xsdType, RosettaXsdMapping xsdMapping, Map<XsdNamedElements, String> rootTypeNames) {
		Data data = xsdMapping.getRosettaTypeFromComplex(xsdType);
		
		// Add supertype
		Optional.of(xsdType)
			.map(XsdComplexType::getSimpleContent)
			.map(XsdSimpleContent::getXsdExtension)
			.map(XsdExtension::getBaseAsComplexType)
			.ifPresent(base -> {
				Data superType = xsdMapping.getRosettaTypeFromComplex(base);
				data.setSuperType(superType);
			});
		
		// Add `[rootType]` annotation if required.
		if (rootTypeNames.containsKey(xsdType)) {
			Annotation rootTypeAnn = builtins.getAnnotationsResource(data.eResource().getResourceSet())
					.getElements().stream()
					.filter(elem -> elem instanceof Annotation)
					.map(elem -> (Annotation)elem)
					.filter(elem -> elem.getName().equals("rootType"))
					.findAny().orElseThrow();
			AnnotationRef rootTypeRef = SimpleFactory.eINSTANCE.createAnnotationRef();
			rootTypeRef.setAnnotation(rootTypeAnn);
			data.getAnnotations().add(rootTypeRef);
		}
		
		// If the complex type extends a simple type, add the corresponding type
		// to the dedicated `value` attribute.
		Optional<XsdSimpleType> baseSimpleType = getBaseSimpleType(xsdType);
		if (baseSimpleType.isPresent()) {
			Attribute attr = xsdMapping.getAttribute(xsdType);
			TypeCall call = attr.getTypeCall();
			RosettaType rosettaType = xsdMapping.getRosettaType(baseSimpleType.get());
			call.setType(rosettaType);
		}
		
		// Add types to attributes based on XSD elements.
		getTypedXsdElements(xsdType)
			.forEach(element -> {
				Attribute attr = xsdMapping.getAttribute(element);
				TypeCall call = attr.getTypeCall();
				RosettaType rosettaType = Optional.of(element)
						.map(XsdElement::getTypeAsXsd)
						.map(xsdMapping::getRosettaType)
						.get();
				call.setType(rosettaType);
			});
		
		// Add types to attributes based on XSD attributes.
		getTypedXsdAttributes(xsdType)
			.forEach(element -> {
				Attribute attr = xsdMapping.getAttribute(element);
				TypeCall call = attr.getTypeCall();
				RosettaType rosettaType = Optional.of(element)
						.map(XsdAttribute::getXsdSimpleType)
						.map(xsdMapping::getRosettaType)
						.get();
				call.setType(rosettaType);
			});
	}
	
	public Optional<TypeXMLConfiguration> getXMLConfiguration(XsdComplexType xsdType, RosettaXsdMapping xsdMapping, Map<XsdNamedElements, String> rootTypeNames, String schemaTargetNamespace) {
		String rootTypeName = rootTypeNames.get(xsdType);
		Map<String, AttributeXMLConfiguration> attributeConfig = getAttributeConfiguration(xsdType, xsdMapping);
		if (rootTypeName == null) {
			if (attributeConfig.isEmpty()) {
				return Optional.empty();
			} else {
				return Optional.of(
						new TypeXMLConfiguration(
							Optional.empty(),
							Optional.empty(),
							Optional.of(attributeConfig)
						));
			}
		}
		Map<String, String> xmlAttributes = new LinkedHashMap<>();
		if (schemaTargetNamespace != null) {
			xmlAttributes.put("xmlns", schemaTargetNamespace);
		}
		xmlAttributes.put("xmlns:xsi", util.XSI_NAMESPACE);
		return Optional.of(
				new TypeXMLConfiguration(
					Optional.of(rootTypeName),
					Optional.of(xmlAttributes),
					attributeConfig.isEmpty() ? Optional.empty() : Optional.of(attributeConfig)
				));
	}
	private Map<String, AttributeXMLConfiguration> getAttributeConfiguration(XsdComplexType xsdType, RosettaXsdMapping xsdMapping) {
		Map<String, AttributeXMLConfiguration> result = new LinkedHashMap<>();
		
		Optional<XsdSimpleType> baseSimpleType = getBaseSimpleType(xsdType);
		if (baseSimpleType.isPresent()) {
			Attribute attr = xsdMapping.getAttribute(xsdType);
			result.put(attr.getName(), new AttributeXMLConfiguration(
					Optional.empty(),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.VALUE)));
		}
		
		getTypedXsdElements(xsdType)
			.forEach(element -> {
				Attribute attr = xsdMapping.getAttribute(element);
				if (!element.getName().equals(attr.getName())) {
					result.put(attr.getName(), new AttributeXMLConfiguration(
							Optional.of(element.getName()),
							Optional.empty(),
							Optional.empty()));
				}
			});
		
		getTypedXsdAttributes(xsdType)
			.forEach(element -> {
				Attribute attr = xsdMapping.getAttribute(element);
				result.put(attr.getName(), new AttributeXMLConfiguration(
						element.getName().equals(attr.getName()) ? Optional.empty() : Optional.of(element.getName()),
						Optional.empty(),
						Optional.of(AttributeXMLRepresentation.ATTRIBUTE)));
			});

		return result;
	}

	private Attribute registerAttribute(XsdElement xsdElement, boolean isChoice, RosettaXsdMapping xsdMapping) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
		util.extractDocs(xsdElement).ifPresent(attribute::setDefinition);

		// name
		attribute.setName(util.allFirstLowerIfNotAbbrevation(xsdElement.getName()));
		
		// type call
		TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
		attribute.setTypeCall(typeCall);

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		if (isChoice) {
			rosettaCardinality.setInf(0);
			// If minOccurs is not equal to the default value 1, then throw.
			if (xsdElement.getMinOccurs() != 1) {
				throw new UnsupportedOperationException("xs:element inside a xs:choice has a non-zero `minOccurs` attribute of " + xsdElement.getMinOccurs() + ".");
			}
		} else {
			rosettaCardinality.setInf(xsdElement.getMinOccurs());
		}
		if (xsdElement.getMaxOccurs().equals(UNBOUNDED)) {
			rosettaCardinality.setUnbounded(true);
		} else {
			rosettaCardinality.setSup(Integer.parseInt(xsdElement.getMaxOccurs()));
		}
		attribute.setCard(rosettaCardinality);
		
		xsdMapping.registerAttribute(xsdElement, attribute);
		
		return attribute;
	}
	private Attribute registerAttribute(XsdAttribute xsdAttribute, RosettaXsdMapping xsdMapping) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
		util.extractDocs(xsdAttribute).ifPresent(attribute::setDefinition);

		// name
		attribute.setName(util.allFirstLowerIfNotAbbrevation(xsdAttribute.getName()));
		
		// type call
		TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
		attribute.setTypeCall(typeCall);

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		if (xsdAttribute.getUse().equals(UsageEnum.REQUIRED.getValue())) {
			rosettaCardinality.setInf(1);
			rosettaCardinality.setSup(1);
		} else if (xsdAttribute.getUse().equals(UsageEnum.OPTIONAL.getValue())) {
			rosettaCardinality.setInf(0);
			rosettaCardinality.setSup(1);
		} else {
			throw new RuntimeException("Unknown XSD attribute usage: " + xsdAttribute.getUse());
		}
		attribute.setCard(rosettaCardinality);
		
		xsdMapping.registerAttribute(xsdAttribute, attribute);
		
		return attribute;
	}
	private Attribute registerValueAttribute(XsdComplexType extendingType, RosettaXsdMapping xsdMapping) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// name
		attribute.setName(SIMPLE_EXTENSION_ATTRIBUTE_NAME);
		
		// type call
		TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
		attribute.setTypeCall(typeCall);

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		rosettaCardinality.setInf(1);
		rosettaCardinality.setSup(1);
		attribute.setCard(rosettaCardinality);
		
		xsdMapping.registerAttribute(extendingType, attribute);
		
		return attribute;
	}
}
