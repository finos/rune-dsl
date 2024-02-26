package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaFactory;

public class XsdEnumImport extends AbstractXsdImport<XsdSimpleType, RosettaEnumeration> {
	
	private final XsdUtil util;

	@Inject
	public XsdEnumImport(XsdUtil util) {
		super(XsdSimpleType.class);
		this.util = util;
	}

	@Override
	public List<XsdSimpleType> filterTypes(List<XsdAbstractElement> elements) {
		return super.filterTypes(elements).stream()
			.filter(x -> util.isEnumType(x))
			.collect(Collectors.toList());
	}

	@Override
	public RosettaEnumeration registerType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		RosettaEnumeration rosettaEnumeration = RosettaFactory.eINSTANCE.createRosettaEnumeration();
		rosettaEnumeration.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(rosettaEnumeration::setDefinition);
		typeMappings.registerEnumType(xsdType, rosettaEnumeration);
		
		List<XsdEnumeration> enumeration = xsdType.getAllRestrictions().stream().flatMap(r -> r.getEnumeration().stream()).toList();

		enumeration.stream()
			.map(e -> this.registerEnumValue(e, typeMappings))
			.forEach(rosettaEnumeration.getEnumValues()::add);
		
		return rosettaEnumeration;
	}

	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
		
	}

	private RosettaEnumValue registerEnumValue(XsdEnumeration ev, RosettaXsdMapping typeMappings) {
		String value = ev.getValue();
		RosettaEnumValue rosettaEnumValue = RosettaFactory.eINSTANCE.createRosettaEnumValue();
		rosettaEnumValue.setName(value);
		util.extractDocs(ev).ifPresent(rosettaEnumValue::setDefinition);

		typeMappings.registerEnumValue(ev, rosettaEnumValue);
		
		return rosettaEnumValue;
	}
}
