package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaModel;

public class XsdSynonymImport {

	public static final String SYNONYM = "synonym";

	private final RosettaModelFactory rosettaModelFactory;

	@Inject
	public XsdSynonymImport(RosettaModelFactory rosettaModelFactory) {
		this.rosettaModelFactory = rosettaModelFactory;
	}

	public void generateSynonyms(List<XsdAbstractElement> xsdElements, GenerationProperties properties, List<String> namespaces) {

		RosettaModel rosettaModel = rosettaModelFactory.createRosettaModel(SYNONYM, properties, namespaces);

		RosettaExternalSynonymSource externalSynonymSource = rosettaModelFactory.createExternalSynonymSource(properties.getSynonymSourceName());
		rosettaModel.getElements().add(externalSynonymSource);

		List<XsdComplexType> complexTypes = getComplexTypes(xsdElements);
		complexTypes.stream()
			.map(rosettaModelFactory::createRosettaExternalClass)
			.forEach(e -> externalSynonymSource.getExternalRefs().add(e));


		getEnumSimpleTypes(xsdElements).stream()
			.map(rosettaModelFactory::createRosettaExternalEnum)
			.forEach(e -> externalSynonymSource.getExternalRefs().add(e));
	}

	private List<XsdComplexType> getComplexTypes(List<XsdAbstractElement> elementStream) {
		return elementStream.stream()
			.filter(XsdComplexType.class::isInstance)
			.map(XsdComplexType.class::cast)
			.collect(Collectors.toList());
	}

	private List<XsdSimpleType> getEnumSimpleTypes(List<XsdAbstractElement> elementStream) {
		return elementStream.stream()
			.filter(XsdSimpleType.class::isInstance)
			.map(XsdSimpleType.class::cast)
			.filter(x -> x.getAllRestrictions().stream().anyMatch(e -> e.getEnumeration().size() > 0))
			.collect(Collectors.toList());
	}
}
