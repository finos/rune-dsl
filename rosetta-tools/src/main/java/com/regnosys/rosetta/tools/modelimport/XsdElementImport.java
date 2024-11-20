package com.regnosys.rosetta.tools.modelimport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.serialisation.AttributeXMLConfiguration;
import com.rosetta.util.serialisation.AttributeXMLRepresentation;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

public class XsdElementImport extends AbstractXsdImport<XsdElement, Data>{

	private final XsdUtil util;
	private final RosettaBuiltinsService builtins;
	private final XsdTypeImport typeImport;
	private final ModelIdProvider modelIdProvider;
	
	@Inject
	public XsdElementImport(XsdUtil util, RosettaBuiltinsService builtins, XsdTypeImport typeImport, ModelIdProvider modelIdProvider) {
		super(XsdElement.class);
		this.util = util;
		this.builtins = builtins;
		this.typeImport = typeImport;
		this.modelIdProvider = modelIdProvider;
	}

	@Override
	public Data registerType(XsdElement xsdElement, RosettaXsdMapping typeMappings, ImportTargetConfig targetConfig) {
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
        return getData(xsdElement, typeMappings, xsdType, targetConfig);
	}

	private Data getData(XsdElement xsdElement, RosettaXsdMapping typeMappings, XsdNamedElements xsdType, ImportTargetConfig targetConfig) {
		String name = util.toTypeName(xsdElement.getName(), targetConfig);
		if (xsdType != null /* TODO */ && typeMappings.hasType(xsdType)) {
			if (xsdType instanceof XsdComplexType) {
				Data dataType = typeMappings.getRosettaTypeFromComplex((XsdComplexType) xsdType);

				if (name.equalsIgnoreCase(dataType.getName())) {
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
					data.setName(name);
					util.extractDocs(xsdElement).ifPresent(data::setDefinition);
					typeMappings.registerElement(xsdElement, data);

					return data;
				}
			} else {
				Data data = SimpleFactory.eINSTANCE.createData();
				data.setName(name);
				util.extractDocs(xsdElement).ifPresent(data::setDefinition);
				typeMappings.registerElement(xsdElement, data);

				Attribute valueAttr = typeImport.createValueAttribute(targetConfig);
				typeMappings.registerAttribute(xsdElement, valueAttr);
				data.getAttributes().add(valueAttr);

				return data;
			}
		} else {
			Data data = SimpleFactory.eINSTANCE.createData();
			data.setName(name);
			util.extractDocs(xsdElement).ifPresent(data::setDefinition);
			typeMappings.registerElement(xsdElement, data);

			return data;
		}
	}
	
	private boolean isRoot(XsdElement xsdElement) {
		return !xsdElement.isAbstractObj() && (xsdElement.getXsdSubstitutionGroup() == null || isRoot(xsdElement.getXsdSubstitutionGroup()));
	}

	@Override
	public void completeType(XsdElement xsdElement, RosettaXsdMapping typeMappings) {
		Data data = typeMappings.getRosettaTypeFromElement(xsdElement);
		if (xsdElement.getTypeAsXsd() == null) {
			// TODO
			return;
		}
		
		if (isRoot(xsdElement)) {
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
		}
		
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
		if (xsdType instanceof XsdComplexType) {
			Data dataType = typeMappings.getRosettaTypeFromComplex((XsdComplexType) xsdType);
			
			if (data.equals(dataType)) {
				// In case the element and type name overlap, we only generate the element.
				// Completed by `XsdTypeImport`
			} else {				
				data.setSuperType(dataType);
			}
		} else {
			// If the type of this element is not complex, add the type to the dedicated `value` attribute.
			Attribute attr = typeMappings.getAttribute(xsdElement);
			attr.setTypeCall(typeMappings.getRosettaTypeCall(xsdType));
		}
	}
	
	public Map<Data, TypeXMLConfiguration> getXMLConfiguration(XsdElement xsdElement, RosettaXsdMapping xsdMapping, String schemaTargetNamespace) {
		Data data = xsdMapping.getRosettaTypeFromElement(xsdElement);
		if (xsdElement.getTypeAsXsd() == null) {
			// TODO
			return Collections.emptyMap();
		}
		
		Map<Data, TypeXMLConfiguration> result = new LinkedHashMap<>();
		
		Optional<ModelSymbolId> substitution = Optional.ofNullable(xsdElement.getXsdSubstitutionGroup()).map(elem -> modelIdProvider.getSymbolId(xsdMapping.getRosettaTypeFromElement(elem)));
		Optional<String> elementName = xsdElement.isAbstractObj() ? Optional.empty() : Optional.of(xsdElement.getName());
		Optional<Map<String, String>> xmlAttributes;
		if (isRoot(xsdElement)) {
			Map<String, String> attrs = new LinkedHashMap<>();
			if (schemaTargetNamespace != null) {
				attrs.put("xmlns", schemaTargetNamespace);
			}
			attrs.put("xmlns:xsi", util.XSI_NAMESPACE);
			xmlAttributes = Optional.of(attrs);
		} else {
			xmlAttributes = Optional.empty();
		}
		Map<String, AttributeXMLConfiguration> attributeConfig = new LinkedHashMap<>();
		result.put(data,
				new TypeXMLConfiguration(
					substitution,
					elementName,
					xmlAttributes,
					Optional.of(attributeConfig),
					Optional.empty()
				));
		
		XsdNamedElements xsdType = xsdElement.getTypeAsXsd();
		if (xsdType instanceof XsdComplexType) {
			Data dataType = xsdMapping.getRosettaTypeFromComplex(xsdType);
			if (data.equals(dataType)) {
				typeImport.completeAttributeConfiguration(attributeConfig, xsdType, xsdMapping, result);
			}
		} else {
			Attribute attr = xsdMapping.getAttribute(xsdElement);
			attributeConfig.put(attr.getName(), new AttributeXMLConfiguration(
					Optional.empty(),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.VALUE)));
		}

		return result;
	}
}
