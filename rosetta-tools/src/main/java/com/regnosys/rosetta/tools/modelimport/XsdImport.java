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
import org.xmlet.xsdparser.xsdelements.XsdSchema;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
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
		
		// First register all rosetta types and attributes, which makes it possible to support
		// forward references and self-references.
		xsdMapping.initializeBuiltinTypeMap(rosettaModelFactory.getResourceSet());
		List<? extends RosettaRootElement> enums = xsdEnumImport.registerTypes(xsdElements, xsdMapping, properties);
		List<? extends RosettaRootElement> aliases = xsdTypeAliasImport.registerTypes(xsdElements, xsdMapping, properties);
		List<? extends RosettaRootElement> types = xsdTypeImport.registerTypes(xsdElements, xsdMapping, properties);
		List<? extends RosettaRootElement> synSources = xsdSynonymImport.registerTypes(xsdElements, xsdMapping, properties);
		
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
		xsdEnumImport.completeTypes(xsdElements, xsdMapping);
		xsdTypeAliasImport.completeTypes(xsdElements, xsdMapping);
		xsdTypeImport.completeTypes(xsdElements, xsdMapping);
		xsdSynonymImport.completeTypes(xsdElements, xsdMapping);
		
		return rosettaModelFactory.getResourceSet();
	}
	
	public RosettaXMLConfiguration generateXMLConfiguration(XsdSchema schema, GenerationProperties properties) {
		List<XsdAbstractElement> xsdElements = schema.getXsdElements().collect(Collectors.toList());
				
		String targetNamespace = schema.getTargetNamespace();
		Map<RosettaType, String> rootTypeNames =
				xsdElements.stream()
					.filter(XsdElement.class::isInstance)
					.map(XsdElement.class::cast)
					.filter(xsdElement -> xsdElement.getType() != null)
					.collect(Collectors.toMap(
							elem -> xsdMapping.getRosettaType(elem.getTypeAsXsd()),
							XsdElement::getName));
		
		Map<ModelSymbolId, TypeXMLConfiguration> result = new HashMap<>();
		xsdTypeImport.filterTypes(xsdElements).stream()
			.forEach(xsdType -> {
				xsdTypeImport.getXMLConfiguration(xsdType, xsdMapping, targetNamespace, rootTypeNames)
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
}
