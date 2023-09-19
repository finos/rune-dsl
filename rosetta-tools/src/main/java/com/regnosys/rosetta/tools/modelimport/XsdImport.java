package com.regnosys.rosetta.tools.modelimport;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;

public class XsdImport {
	public final String ENUM = "enum";
	public final String TYPE = "type";
	public final String SYNONYM = "synonym";

	private final RosettaModelFactory rosettaModelFactory;
	private final XsdTypeImport xsdTypeImport;
	private final XsdEnumImport xsdEnumImport;
	private final XsdSynonymImport xsdSynonymImport;
	private final XsdTypeAliasImport xsdTypeAliasImport;
	private final RosettaXsdMapping typeMapping;

	@Inject
	public XsdImport(RosettaModelFactory rosettaModelFactory, XsdTypeImport xsdTypeImport, XsdEnumImport xsdEnumImport, XsdSynonymImport xsdSynonymImport, XsdTypeAliasImport xsdTypeAliasImport, RosettaXsdMapping typeMapping) {
		this.rosettaModelFactory = rosettaModelFactory;
		this.xsdTypeImport = xsdTypeImport;
		this.xsdEnumImport = xsdEnumImport;
		this.xsdSynonymImport = xsdSynonymImport;
		this.xsdTypeAliasImport = xsdTypeAliasImport;
		this.typeMapping = typeMapping;
	}

	public ResourceSet generateRosetta(List<XsdAbstractElement> xsdElements, GenerationProperties properties, List<String> namespaces) {
		// First register all rosetta types, which makes it possible to support
		// forward references and self-references.
		typeMapping.initializeBuiltinTypeMap(rosettaModelFactory.getResourceSet());
		List<? extends RosettaRootElement> enums = xsdEnumImport.registerTypes(xsdElements, typeMapping, properties);
		List<? extends RosettaRootElement> aliases = xsdTypeAliasImport.registerTypes(xsdElements, typeMapping, properties);
		List<? extends RosettaRootElement> types = xsdTypeImport.registerTypes(xsdElements, typeMapping, properties);
		List<? extends RosettaRootElement> synSources = xsdSynonymImport.registerTypes(xsdElements, typeMapping, properties);
		
		// Then write these types to the appropriate resources.
		if (enums.size() > 0) {
			RosettaModel enumModel = rosettaModelFactory.createRosettaModel(ENUM, properties, namespaces);
			enumModel.getElements().addAll(enums);
		}
		
		if (aliases.size() > 0 || types.size() > 0) {
			RosettaModel typeModel = rosettaModelFactory.createRosettaModel(TYPE, properties, namespaces);
			typeModel.getElements().addAll(aliases);
			typeModel.getElements().addAll(types);
		}
		
		if (synSources.size() > 0) {
			RosettaModel synonymModel = rosettaModelFactory.createRosettaModel(SYNONYM, properties, namespaces);
			synonymModel.getElements().addAll(synSources);
		}
		
		// Then fill in the contents of these types.
		xsdEnumImport.completeTypes(xsdElements, typeMapping);
		xsdTypeAliasImport.completeTypes(xsdElements, typeMapping);
		xsdTypeImport.completeTypes(xsdElements, typeMapping);
		xsdSynonymImport.completeTypes(xsdElements, typeMapping);
		
		return rosettaModelFactory.getResourceSet();
	}

	public void saveResources(String outputPath) throws IOException {
		rosettaModelFactory.saveResources(outputPath);
	}
}
