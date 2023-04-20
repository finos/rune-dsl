package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import com.regnosys.rosetta.rosetta.RosettaModel;

public class XsdEnumImport {

	public static final String ENUM = "enum";

	private final RosettaModelFactory rosettaModelFactory;

	@Inject
	public XsdEnumImport(RosettaModelFactory rosettaModelFactory) {
		this.rosettaModelFactory = rosettaModelFactory;
	}


	public void generateEnums(List<XsdAbstractElement> xsdElements, GenerationProperties properties, List<String> namespaces) {
		RosettaModel rosettaModel = rosettaModelFactory.createRosettaModel(ENUM, properties, namespaces);
		List<XsdSimpleType> enumSimpleTypes = getEnumSimpleTypes(xsdElements);
		enumSimpleTypes.stream()
			.map(rosettaModelFactory::createRosettaEnumeration)
			.forEach(rosettaModel.getElements()::add);
	}

	private List<XsdSimpleType> getEnumSimpleTypes(List<XsdAbstractElement> elementStream) {
		return elementStream.stream()
			.filter(XsdSimpleType.class::isInstance)
			.map(XsdSimpleType.class::cast)
			.filter(x -> x.getAllRestrictions().stream().anyMatch(e -> e.getEnumeration().size() > 0))
			.collect(Collectors.toList());
	}
}
