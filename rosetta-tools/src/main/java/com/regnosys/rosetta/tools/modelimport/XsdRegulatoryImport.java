package com.regnosys.rosetta.tools.modelimport;

import java.util.List;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;

import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaSegment;

@Deprecated
public class XsdRegulatoryImport extends AbstractXsdImport<XsdAbstractElement, RosettaRootElement> {
	
	@Inject
	public XsdRegulatoryImport() {
		super(XsdAbstractElement.class);
	}

	@Override
	public RosettaRootElement registerType(XsdAbstractElement xsdType, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		return null;
	}
	@Override
	public List<? extends RosettaRootElement> registerTypes(List<XsdAbstractElement> xsdElements, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		RosettaBody body = RosettaFactory.eINSTANCE.createRosettaBody();
		body.setBodyType(properties.getBodyType());
		body.setDefinition(properties.getBodyDefinition());
		body.setName(properties.getBodyName());
		typeMappings.registerBody(body);
		
		RosettaCorpus corpus = RosettaFactory.eINSTANCE.createRosettaCorpus();
		corpus.setCorpusType(properties.getCorpusType());
		corpus.setName(properties.getCorpusName());
		corpus.setDisplayName(properties.getCorpusDisplayName());
		corpus.setDefinition(properties.getCorpusDefinition());
		typeMappings.registerCorpus(corpus);
		
		RosettaSegment segment = RosettaFactory.eINSTANCE.createRosettaSegment();
		segment.setName(properties.getSegmentName());
		typeMappings.registerSegment(segment);
		
		return List.of(body, corpus, segment);
	}

	@Override
	public void completeType(XsdAbstractElement xsdType, RosettaXsdMapping typeMappings) {
		
	}
	@Override
	public void completeTypes(List<XsdAbstractElement> xsdElements, RosettaXsdMapping typeMappings) {
		RosettaCorpus corpus = typeMappings.getCorpus();
		corpus.setBody(typeMappings.getBody());
	}
}
