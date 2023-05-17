package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdExtension;
import org.xmlet.xsdparser.xsdelements.XsdSimpleContent;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaSegment;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;

public class XsdTypeImport extends AbstractXsdImport<XsdComplexType, Data> {
	public final String UNBOUNDED = "unbounded";

	private final XsdUtil util;
	
	@Inject
	public XsdTypeImport(XsdUtil util) {
		super(XsdComplexType.class);
		this.util = util;
	}

	@Override
	public Data registerType(XsdComplexType xsdType, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		Data data = SimpleFactory.eINSTANCE.createData();
		data.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(data::setDefinition);
		typeMappings.registerComplexType(xsdType, data);
		return data;
	}

	@Override
	public void completeType(XsdComplexType xsdType, RosettaXsdMapping typeMappings) {
		Data data = typeMappings.getRosettaTypeFromComplex(xsdType);
		
		// add supertype
		Optional.of(xsdType)
			.map(XsdComplexType::getSimpleContent)
			.map(XsdSimpleContent::getXsdExtension)
			.map(XsdExtension::getBaseAsComplexType)
			.ifPresent(base -> {
				Data superType = typeMappings.getRosettaTypeFromComplex(base);
				data.setSuperType(superType);
			});
		
		// add attributes
		Optional.of(xsdType)
			.map(XsdComplexType::getElements).stream()
			.flatMap(List::stream)
			.map(ReferenceBase::getElement)
			.filter(XsdElement.class::isInstance)
			.map(XsdElement.class::cast)
			.filter(xsdElement -> xsdElement.getType() != null)
			.map(element -> createAttribute(element, typeMappings))
			.forEach(data.getAttributes()::add);
	}
	
	private Attribute createAttribute(XsdElement element, RosettaXsdMapping typeMappings) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
		util.extractDocs(element).ifPresent(attribute::setDefinition);

		// name
		attribute.setName(StringExtensions.toFirstLower(element.getName()));
		
		// type
		RosettaType rosettaType = Optional.of(element)
				.map(XsdElement::getTypeAsXsd)
				.map(typeMappings::getRosettaType)
				.get();
		TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
		typeCall.setType(rosettaType);
		attribute.setTypeCall(typeCall);

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		rosettaCardinality.setInf(element.getMinOccurs());
		if (element.getMaxOccurs().equals(UNBOUNDED)) {
			rosettaCardinality.setUnbounded(true);
		} else {
			rosettaCardinality.setSup(Integer.parseInt(element.getMaxOccurs()));
		}
		attribute.setCard(rosettaCardinality);

		// docReference
//		RosettaBody body = typeMappings.getBody();
//		RosettaCorpus corpus = typeMappings.getCorpus();
//		RosettaSegment segment = typeMappings.getSegment();
//		Optional.ofNullable(element.getTypeAsSimpleType())
//			.map(xsdName -> createRosettaDocReference(xsdName.getName(), body, corpus, segment, util.extractDocs(xsdName)))
//			.ifPresent(attribute.getReferences()::add);
		
		return attribute;
	}
	
	private RosettaDocReference createRosettaDocReference(String xsdName, RosettaBody rosettaBody, RosettaCorpus rosettaCorpus, RosettaSegment rosettaSegment, Optional<String> provision) {
		RosettaDocReference rosettaDocReference = RosettaFactory.eINSTANCE.createRosettaDocReference();

		RegulatoryDocumentReference regulatoryDocumentReference = RosettaFactory.eINSTANCE.createRegulatoryDocumentReference();
		regulatoryDocumentReference.setBody(rosettaBody);
		regulatoryDocumentReference.getCorpuses().add(rosettaCorpus);
		rosettaDocReference.setDocReference(regulatoryDocumentReference);

		provision.ifPresent(rosettaDocReference::setProvision);

		RosettaSegmentRef rosettaSegmentRef = RosettaFactory.eINSTANCE.createRosettaSegmentRef();
		rosettaSegmentRef.setSegment(rosettaSegment);
		rosettaSegmentRef.setSegmentRef(xsdName);
		regulatoryDocumentReference.getSegments().add(rosettaSegmentRef);

		return rosettaDocReference;
	}
}
