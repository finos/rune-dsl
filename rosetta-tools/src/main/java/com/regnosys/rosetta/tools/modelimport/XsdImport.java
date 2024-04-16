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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdSchema;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.serialisation.RosettaXMLConfiguration;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

public class XsdImport {
	public final String ENUM = "enum";
	public final String TYPE = "type";
	public final String SYNONYM = "synonym";

	private final RosettaModelFactory rosettaModelFactory;
	private final XsdTypeImport xsdTypeImport;
	private final XsdEnumImport xsdEnumImport;
	private final XsdSynonymImport xsdSynonymImport;
	private final XsdTypeAliasImport xsdTypeAliasImport;
	private final RosettaXsdMapping xsdMapping;

	@Inject
	public XsdImport(RosettaModelFactory rosettaModelFactory, XsdTypeImport xsdTypeImport, XsdEnumImport xsdEnumImport, XsdSynonymImport xsdSynonymImport, XsdTypeAliasImport xsdTypeAliasImport, RosettaXsdMapping xsdMapping) {
		this.rosettaModelFactory = rosettaModelFactory;
		this.xsdTypeImport = xsdTypeImport;
		this.xsdEnumImport = xsdEnumImport;
		this.xsdSynonymImport = xsdSynonymImport;
		this.xsdTypeAliasImport = xsdTypeAliasImport;
		this.xsdMapping = xsdMapping;
	}

	public ResourceSet generateRosetta(XsdSchema schema, GenerationProperties properties) {
		List<XsdAbstractElement> xsdElements = schema.getXsdElements().collect(Collectors.toList());
		
		// Initialization
		xsdMapping.initializeBuiltins(rosettaModelFactory.getResourceSet());
		Map<XsdNamedElements, String> rootTypeNames = getRootTypeNames(xsdElements);
		
		// First register all rosetta types and attributes, which makes it possible to support
		// forward references and self-references.
		List<? extends RosettaRootElement> enums = xsdEnumImport.registerTypes(xsdElements, xsdMapping, rootTypeNames, properties);
		List<? extends RosettaRootElement> aliases = xsdTypeAliasImport.registerTypes(xsdElements, xsdMapping, rootTypeNames, properties);
		List<? extends RosettaRootElement> types = xsdTypeImport.registerTypes(xsdElements, xsdMapping, rootTypeNames, properties);
		List<? extends RosettaRootElement> synSources = xsdSynonymImport.registerTypes(xsdElements, xsdMapping, rootTypeNames, properties);
		
		// Then write these types to the appropriate resources.
		if (enums.size() > 0) {
			RosettaModel enumModel = rosettaModelFactory.createRosettaModel(ENUM, properties);
			enumModel.getElements().addAll(enums);
		}
		
		if (aliases.size() > 0 || types.size() > 0) {
			RosettaModel typeModel = rosettaModelFactory.createRosettaModel(TYPE, properties);
			typeModel.getElements().addAll(aliases);
			typeModel.getElements().addAll(types);
		}
		
		if (synSources.size() > 0) {
			RosettaModel synonymModel = rosettaModelFactory.createRosettaModel(SYNONYM, properties);
			synonymModel.getElements().addAll(synSources);
		}
		
		// Then fill in the contents of these types.
		xsdEnumImport.completeTypes(xsdElements, xsdMapping, rootTypeNames);
		xsdTypeAliasImport.completeTypes(xsdElements, xsdMapping, rootTypeNames);
		xsdTypeImport.completeTypes(xsdElements, xsdMapping, rootTypeNames);
		xsdSynonymImport.completeTypes(xsdElements, xsdMapping, rootTypeNames);
		
		return rosettaModelFactory.getResourceSet();
	}
	
	public RosettaXMLConfiguration generateXMLConfiguration(XsdSchema schema, GenerationProperties properties) {
		List<XsdAbstractElement> xsdElements = schema.getXsdElements().collect(Collectors.toList());
		
		Map<XsdNamedElements, String> rootTypeNames = getRootTypeNames(xsdElements);
		String targetNamespace = schema.getTargetNamespace();
		
		Map<ModelSymbolId, TypeXMLConfiguration> result = new HashMap<>();
		xsdTypeImport.filterTypes(xsdElements).stream()
			.forEach(xsdType -> {
				xsdTypeImport.getXMLConfiguration(xsdType, xsdMapping, rootTypeNames, targetNamespace)
					.ifPresent(typeXMLConfig -> {
						Data type = xsdMapping.getRosettaTypeFromComplex(xsdType);
						result.put(new RDataType(type).getSymbolId(), typeXMLConfig);
					});
			});
		return new RosettaXMLConfiguration(result);
	}

	public void saveResources(String outputPath) throws IOException {
		rosettaModelFactory.saveResources(outputPath);
	}
	
	private Map<XsdNamedElements, String> getRootTypeNames(List<XsdAbstractElement> xsdElements) {
		return xsdElements.stream()
			.filter(XsdElement.class::isInstance)
			.map(XsdElement.class::cast)
			.filter(xsdElement -> xsdElement.getType() != null)
			.collect(Collectors.toMap(
					elem -> elem.getTypeAsXsd(),
					XsdElement::getName));
	}
}
