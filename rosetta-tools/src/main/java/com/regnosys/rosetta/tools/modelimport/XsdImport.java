package com.regnosys.rosetta.tools.modelimport;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;

public class XsdImport {

	private final RosettaModelFactory rosettaModelFactory;
	private final XsdTypeImport xsdTypeImport;
	private final XsdEnumImport xsdEnumImport;
	private final XsdSynonymImport xsdSynonymImport;

	@Inject
	public XsdImport(RosettaModelFactory rosettaModelFactory, XsdTypeImport xsdTypeImport, XsdEnumImport xsdEnumImport, XsdSynonymImport xsdSynonymImport) {
		this.rosettaModelFactory = rosettaModelFactory;
		this.xsdTypeImport = xsdTypeImport;
		this.xsdEnumImport = xsdEnumImport;
		this.xsdSynonymImport = xsdSynonymImport;
	}

	public void generateRosetta(List<XsdAbstractElement> xsdElements, GenerationProperties properties, List<String> namespaces) {
		xsdEnumImport.generateEnums(xsdElements, properties, namespaces);
		xsdTypeImport.generateTypes(xsdElements, properties, namespaces);
		xsdSynonymImport.generateSynonyms(xsdElements, properties, namespaces);
		
	}

	public void saveResources(String outputPath) throws IOException {
		rosettaModelFactory.saveResources(outputPath);
	}
}
