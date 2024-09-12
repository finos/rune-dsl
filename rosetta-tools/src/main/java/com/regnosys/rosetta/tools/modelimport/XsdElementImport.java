package com.regnosys.rosetta.tools.modelimport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.rosetta.util.serialisation.AttributeXMLConfiguration;
import com.rosetta.util.serialisation.AttributeXMLRepresentation;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

public class XsdElementImport extends AbstractXsdImport<XsdElement, Data>{

	private final XsdUtil util;
	private final RosettaBuiltinsService builtins;
	private final XsdTypeImport typeImport;
	
	@Inject
	public XsdElementImport(XsdUtil util, RosettaBuiltinsService builtins, XsdTypeImport typeImport) {
		super(XsdElement.class);
		this.util = util;
		this.builtins = builtins;
		this.typeImport = typeImport;
	}

	@Override
	public Data registerType(XsdElement xsdElement, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
        return getData(xsdElement, typeMappings, xsdType);
	}

	private Data getData(XsdElement xsdElement, RosettaXsdMapping typeMappings, XsdNamedElements xsdType) {
		if (xsdType != null /* TODO */ && typeMappings.hasType(xsdType)) {
			if (xsdType instanceof XsdComplexType) {
				Data dataType = typeMappings.getRosettaTypeFromComplex((XsdComplexType) xsdType);

				String name = StringUtils.capitalize(xsdElement.getName());
				if (name.equals(dataType.getName())) {
					// In case the element and type name overlap, we only generate the element.
					// Join the documentation.
					util.extractDocs(xsdElement).ifPresent(elemDocs -> {
						if (dataType.getDefinition() == null) {
							dataType.setDefinition(elemDocs);
						} else {
							dataType.setDefinition(elemDocs + " " + dataType.getDefinition());
						}
					});
					typeMappings.registerElement(xsdElement, dataType);

					return dataType;
				} else {
					Data data = SimpleFactory.eINSTANCE.createData();
					data.setName(xsdElement.getName());
					util.extractDocs(xsdElement).ifPresent(data::setDefinition);
					typeMappings.registerElement(xsdElement, data);

					return data;
				}
			} else {
				Data data = SimpleFactory.eINSTANCE.createData();
				data.setName(xsdElement.getName());
				util.extractDocs(xsdElement).ifPresent(data::setDefinition);
				typeMappings.registerElement(xsdElement, data);

				Attribute valueAttr = typeImport.createValueAttribute();
				typeMappings.registerAttribute(xsdElement, valueAttr);
				data.getAttributes().add(valueAttr);

				return data;
			}
		} else {
			Data data = SimpleFactory.eINSTANCE.createData();
			data.setName(StringUtils.capitalize(xsdElement.getName()));
			util.extractDocs(xsdElement).ifPresent(data::setDefinition);
			typeMappings.registerElement(xsdElement, data);

			return data;
		}
	}

	@Override
	public void completeType(XsdElement xsdElement, RosettaXsdMapping typeMappings) {
		Data data = typeMappings.getRosettaTypeFromElement(xsdElement);
		if (xsdElement.getTypeAsXsd() == null) {
			// TODO
			return;
		}
		
		// Add [rootType] annotation
		Annotation rootTypeAnn = builtins.getAnnotationsResource(data.eResource().getResourceSet())
				.getElements().stream()
				.filter(elem -> elem instanceof Annotation)
				.map(elem -> (Annotation)elem)
				.filter(elem -> elem.getName().equals("rootType"))
				.findAny().orElseThrow();
		AnnotationRef rootTypeRef = SimpleFactory.eINSTANCE.createAnnotationRef();
		rootTypeRef.setAnnotation(rootTypeAnn);
		data.getAnnotations().add(rootTypeRef);
		
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
		if (xsdType instanceof XsdComplexType) {
			Data dataType = typeMappings.getRosettaTypeFromComplex((XsdComplexType) xsdType);
			
			if (data.equals(dataType)) {
				// In case the element and type name overlap, we only generate the element.
				// Completed by `XsdTypeImport`
			} else {
				TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
				typeCall.setType(dataType);
				data.setSuperType(typeCall);
			}
		} else {
			// If the type of this element is not complex, add the type to the dedicated `value` attribute.
			Attribute attr = typeMappings.getAttribute(xsdElement);
			attr.setTypeCall(typeMappings.getRosettaTypeCall(xsdType));
		}
	}
	
	public Optional<TypeXMLConfiguration> getXMLConfiguration(XsdElement xsdElement, RosettaXsdMapping typeMappings, String schemaTargetNamespace) {
		Data data = typeMappings.getRosettaTypeFromElement(xsdElement);
		if (xsdElement.getTypeAsXsd() == null) {
			// TODO
			return Optional.empty();
		}
		
		Map<String, AttributeXMLConfiguration> attributeConfig;
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
		if (xsdType instanceof XsdComplexType) {
			Data dataType = typeMappings.getRosettaTypeFromComplex((XsdComplexType) xsdType);
			if (data.equals(dataType)) {
				attributeConfig = typeImport.getAttributeConfiguration((XsdComplexType) xsdElement.getTypeAsXsd(), typeMappings);
			} else {				
				attributeConfig = Collections.emptyMap();
			}
		} else {
			attributeConfig = new LinkedHashMap<>();
			Attribute attr = typeMappings.getAttribute(xsdElement);
			attributeConfig.put(attr.getName(), new AttributeXMLConfiguration(
					Optional.empty(),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.VALUE)));
		}
		
		Map<String, String> xmlAttributes = new LinkedHashMap<>();
		if (schemaTargetNamespace != null) {
			xmlAttributes.put("xmlns", schemaTargetNamespace);
		}
		xmlAttributes.put("xmlns:xsi", util.XSI_NAMESPACE);
		return Optional.of(
				new TypeXMLConfiguration(
					Optional.of(xsdElement.getName()),
					Optional.of(xmlAttributes),
					attributeConfig.isEmpty() ? Optional.empty() : Optional.of(attributeConfig)
				));
	}
}
