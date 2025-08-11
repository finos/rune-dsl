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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.*;
import jakarta.inject.Inject;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.serialisation.RosettaXMLConfiguration;
import com.rosetta.util.serialisation.TypeXMLConfiguration;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

public class XsdImport {
	public final String ENUM = "enum";
	public final String TYPE = "type";

	private final RosettaModelFactory rosettaModelFactory;
	private final XsdElementImport xsdElementImport;
	private final XsdTypeImport xsdTypeImport;
	private final XsdEnumImport xsdEnumImport;
	private final XsdTypeAliasImport xsdTypeAliasImport;
	private final XsdChoiceImport xsdChoiceImport;
	private final RosettaXsdMapping xsdMapping;
	private final ModelIdProvider modelIdProvider;

	@Inject
	public XsdImport(RosettaModelFactory rosettaModelFactory, XsdElementImport xsdElementImport, XsdTypeImport xsdTypeImport, XsdEnumImport xsdEnumImport, XsdTypeAliasImport xsdTypeAliasImport, XsdChoiceImport xsdChoiceImport, RosettaXsdMapping xsdMapping, ModelIdProvider modelIdProvider) {
		this.rosettaModelFactory = rosettaModelFactory;
		this.xsdElementImport = xsdElementImport;
		this.xsdTypeImport = xsdTypeImport;
		this.xsdEnumImport = xsdEnumImport;
		this.xsdTypeAliasImport = xsdTypeAliasImport;
		this.xsdChoiceImport = xsdChoiceImport;
		this.xsdMapping = xsdMapping;
		this.modelIdProvider = modelIdProvider;
	}

	public ResourceSet generateRosetta(RosettaXsdParser parsedInstance, ImportTargetConfig targetConfig) {
		List<XsdAbstractElement> xsdElements = parsedInstance.getResultXsdSchemas().flatMap(XsdSchema::getXsdElements).toList();

		xsdElements = promoteInlineTypes(xsdElements);

		// Initialization
		xsdMapping.initializeBuiltins(rosettaModelFactory.getResourceSet(), targetConfig);
		
		// First register all rosetta types and attributes, which makes it possible to support
		// forward references and self-references.
		List<? extends RosettaRootElement> enums = xsdEnumImport.registerTypes(xsdElements, xsdMapping, targetConfig);
		List<? extends RosettaRootElement> aliases = xsdTypeAliasImport.registerTypes(xsdElements, xsdMapping, targetConfig);
		List<? extends RosettaRootElement> choices = xsdChoiceImport.registerTypes(xsdElements, xsdMapping, targetConfig);
		List<? extends Data> elements = xsdElementImport.registerTypes(xsdElements, xsdMapping, targetConfig);
		List<? extends Data> types = xsdTypeImport.registerTypes(xsdElements, xsdMapping, targetConfig)
				.stream().flatMap(Collection::stream).toList();

        // Post process to circumvent name conflicts:
        Set<String> elementNames = elements.stream().map(RosettaNamed::getName).collect(Collectors.toSet());
        Set<String> typeNames = types.stream().map(RosettaNamed::getName).collect(Collectors.toSet());
        types.stream().filter(t -> elementNames.contains(t.getName())).forEach(t -> {
            String newName = t.getName() + "Type";
            if (!typeNames.contains(newName)) {
                t.setName(t.getName() + "Type");
            } else {
                elements.stream().filter(e -> e.getName().equals(t.getName())).findAny().ifPresent(elem -> elem.setName(t.getName() + "Element"));
            }
        });

		// Then write these types to the appropriate resources.
		if (!enums.isEmpty()) {
			RosettaModel enumModel = rosettaModelFactory.createRosettaModel(ENUM, targetConfig);
			enumModel.getElements().addAll(enums);
		}
		
		if (!aliases.isEmpty() || !elements.isEmpty() || !types.isEmpty() || !choices.isEmpty()) {
			RosettaModel typeModel = rosettaModelFactory.createRosettaModel(TYPE, targetConfig);
			typeModel.getElements().addAll(aliases);
			typeModel.getElements().addAll(elements);
			typeModel.getElements().addAll(types);
			typeModel.getElements().addAll(choices);
		}
		
		// Then fill in the contents of these types.
		xsdEnumImport.completeTypes(xsdElements, xsdMapping);
		xsdTypeAliasImport.completeTypes(xsdElements, xsdMapping);
		xsdChoiceImport.completeTypes(xsdElements, xsdMapping);
		xsdElementImport.completeTypes(xsdElements, xsdMapping);
		xsdTypeImport.completeTypes(xsdElements, xsdMapping);

		return rosettaModelFactory.getResourceSet();
	}

	private List<XsdAbstractElement> promoteInlineTypes(List<XsdAbstractElement> elements) {
		List<XsdAbstractElement> result = new ArrayList<>();

		for (XsdAbstractElement elem : elements) {
			if (elem instanceof XsdElement xsdElement) {
				if (xsdElement.getXsdComplexType() != null){
					XsdComplexType inlineType = xsdElement.getXsdComplexType();
					if (((XsdElement) elem).getType() == null && inlineType != null) {
						inlineType.setName(xsdElement.getRawName());
						result.add(inlineType);
						result.addAll(promoteInlineTypesRecursively(getChildElements(inlineType)));
						continue;
					}
				}
				else if(xsdElement.getXsdSimpleType() != null) {
					XsdSimpleType inlineType = xsdElement.getXsdSimpleType();
					if (((XsdElement) elem).getType() == null && inlineType != null) {
						inlineType.setName(xsdElement.getRawName());
						result.add(inlineType);
						result.addAll(promoteInlineTypesRecursively(getChildElements(inlineType)));
						continue;
					}
				}

			}
			result.add(elem);
			result.addAll(promoteInlineTypesRecursively(getChildElements(elem)));
		}
		return result;
	}

	private List<XsdAbstractElement> promoteInlineTypesRecursively(List<XsdAbstractElement> elements) {
		List<XsdAbstractElement> result = new ArrayList<>();
		for (XsdAbstractElement elem : elements) {
			if (elem instanceof XsdElement xsdElement) {
				XsdComplexType inlineType = xsdElement.getXsdComplexType();
				if (((XsdElement) elem).getType() == null && inlineType != null) {
					if (inlineType.getXsdChildElement() != null && inlineType.getChildAsGroup() == null) {
						inlineType.setName(xsdElement.getRawName());
						result.add(inlineType);
						result.addAll(promoteInlineTypesRecursively(getChildElements(inlineType)));
					}
				}
			}
		}
		return result;
	}

	private List<XsdAbstractElement> getChildElements(XsdAbstractElement element) {
		if (element == null) return Collections.emptyList();
		List<XsdAbstractElement> childElements = new ArrayList<>();
		List<ReferenceBase> refs = element.getElements();

		if (refs != null) {
			for (ReferenceBase ref : refs) {
				XsdAbstractElement el = ref.getElement();
				if (el instanceof XsdElement) {
					childElements.add(el);
				} else {
					childElements.addAll(getChildElements(el));
				}
			}
		} else if (element instanceof XsdComplexType complexType) {
			//check for complex/simple content, check for extension inside
			if (complexType.getComplexContent() != null) {
				XsdComplexContent content = complexType.getComplexContent();
				childElements.addAll(getChildElements(content));

				if (content.getXsdExtension() != null) {
					childElements.addAll(getChildElements(content.getXsdExtension()));
				}
			} else if (complexType.getSimpleContent() != null){
				XsdSimpleContent content = complexType.getSimpleContent();
				childElements.addAll(getChildElements(content));

				if (content.getXsdExtension() != null) {
					childElements.addAll(getChildElements(content.getXsdExtension()));
				}
			} else {
				//TODO: Find other cases?
			}

		}
		return childElements;
	}

	public RosettaXMLConfiguration generateXMLConfiguration(XsdParser parsedInstance, ImportTargetConfig targetConfig) {
		Map<String, List<XsdAbstractElement>> targetNamespaceToXsdElementsMap =
			parsedInstance.getResultXsdSchemas()
				.flatMap(schema -> {
					// Update elements, pair with schema's namespace
					List<XsdAbstractElement> promotedElements = promoteInlineTypes(schema.getXsdElements().toList());
					return promotedElements.stream()
							.map(element -> new AbstractMap.SimpleEntry<>(schema.getTargetNamespace(), element));
				})
				.collect(Collectors.groupingBy(
						entry -> Optional.ofNullable(entry.getKey()).orElse("__NULL__"),
						Collectors.mapping(Map.Entry::getValue,
								Collectors.toList())
				));
				
		Map<ModelSymbolId, TypeXMLConfiguration> result = new HashMap<>();
		targetNamespaceToXsdElementsMap.forEach((targetNamespace, xsdElements) -> {
			Streams.concat(
					xsdEnumImport.filterTypes(xsdElements).stream().flatMap(x -> xsdEnumImport.getXMLConfiguration(x, xsdMapping, targetNamespace).entrySet().stream()),
					xsdElementImport.filterTypes(xsdElements).stream().flatMap(x -> xsdElementImport.getXMLConfiguration(x, xsdMapping, targetNamespace).entrySet().stream()),
					xsdTypeImport.filterTypes(xsdElements).stream().flatMap(x -> xsdTypeImport.getXMLConfiguration(x, xsdMapping, targetNamespace).entrySet().stream())
				)
				.filter(e -> !isEmpty(e.getValue()))
				.map(e -> Map.entry(e.getKey(), prune(e.getValue())))
				.collect(Collectors.toMap(e -> modelIdProvider.getSymbolId(e.getKey()), Map.Entry::getValue))
				.forEach((id, config) -> result.put(id, config));
		});
		return new RosettaXMLConfiguration(result);
	}
	private TypeXMLConfiguration prune(TypeXMLConfiguration config) {
		return new TypeXMLConfiguration(
				config.getSubstitutionGroup(),
				config.getXmlElementName(),
				config.getXmlElementFullyQualifiedName(),
				config.getAbstract(),
				config.getXmlAttributes().map(x -> x.isEmpty() ? null : x),
				config.getAttributes().map(x -> x.isEmpty() ? null : x),
				config.getEnumValues().map(x -> x.isEmpty() ? null : x)
			);
	}
	private boolean isEmpty(TypeXMLConfiguration config) {
		return config.getSubstitutionGroup().isEmpty()
				&& config.getXmlElementName().isEmpty()
				&& (config.getXmlAttributes().isEmpty() || config.getXmlAttributes().get().isEmpty())
				&& (config.getAttributes().isEmpty() || config.getAttributes().get().isEmpty())
				&& (config.getEnumValues().isEmpty() || config.getEnumValues().get().isEmpty());
	}

	public void saveResources(String outputPath) throws IOException {
		rosettaModelFactory.saveResources(outputPath);
	}
}
