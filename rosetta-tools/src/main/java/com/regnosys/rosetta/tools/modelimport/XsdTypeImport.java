package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaSegment;

public class XsdTypeImport {

	public static final String TYPE = "type";

	private final RosettaModelFactory rosettaModelFactory;

	@Inject
	public XsdTypeImport(RosettaModelFactory rosettaModelFactory) {
		this.rosettaModelFactory = rosettaModelFactory;
	}

	public void generateTypes(List<XsdAbstractElement> xsdElements, GenerationProperties properties, List<String> namespaces) {

		RosettaModel rosettaModel = rosettaModelFactory.createRosettaModel(TYPE, properties, namespaces);

		RosettaBody body = rosettaModelFactory.createBody(properties.getBodyType(), properties.getBodyName(), properties.getBodyDefinition());
		rosettaModel.getElements().add(body);
		RosettaCorpus corpus = rosettaModelFactory.createCorpus(body, properties.getCorpusType(), properties.getCorpusName(), properties.getCorpusDisplayName(), properties.getCorpusDefinition());
		rosettaModel.getElements().add(corpus);
		RosettaSegment rosettaSegment = rosettaModelFactory.createSegment(properties.getSegmentName());
		rosettaModel.getElements().add(rosettaSegment);

		List<XsdSimpleType> simpleTypes = getSimpleTypes(xsdElements);
		simpleTypes.stream()
			.map(rosettaModelFactory::createData)
			.forEach(rosettaModel.getElements()::add);

		List<XsdComplexType> complexTypes = getComplexTypes(xsdElements);
		complexTypes.stream()
			.map(rosettaModelFactory::createData)
			.forEach(rosettaModel.getElements()::add);


		complexTypes.forEach(rosettaModelFactory::addSuperType);
		complexTypes.forEach(complexType -> rosettaModelFactory.addAttributesToData(complexType, body, corpus, rosettaSegment));

	}


	private List<XsdComplexType> getComplexTypes(List<XsdAbstractElement> elementStream) {
		return elementStream.stream()
			.filter(XsdComplexType.class::isInstance)
			.map(XsdComplexType.class::cast)
			.collect(Collectors.toList());
	}

	private List<XsdSimpleType> getSimpleTypes(List<XsdAbstractElement> elementStream) {
		return elementStream.stream()
			.filter(XsdSimpleType.class::isInstance)
			.map(XsdSimpleType.class::cast)
			.filter(x -> x.getAllRestrictions().stream().anyMatch(e -> e.getEnumeration().size() == 0))
			.collect(Collectors.toList());
	}
}
