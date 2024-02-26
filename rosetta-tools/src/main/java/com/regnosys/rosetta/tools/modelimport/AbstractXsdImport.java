package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.stream.Collectors;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;

import com.regnosys.rosetta.rosetta.RosettaRootElement;

public abstract class AbstractXsdImport<XsdType extends XsdAbstractElement, Result> {
	private final Class<XsdType> xsdType;
	public AbstractXsdImport(Class<XsdType> xsdType) {
		this.xsdType = xsdType;
	}

	public List<XsdType> filterTypes(List<XsdAbstractElement> elements) {
		return elements.stream()
				.filter(xsdType::isInstance)
				.map(xsdType::cast)
				.collect(Collectors.toList());
	}
	public abstract Result registerType(XsdType xsdType, RosettaXsdMapping typeMappings, GenerationProperties properties);
	public abstract void completeType(XsdType xsdType, RosettaXsdMapping typeMappings);
	public List<? extends Result> registerTypes(List<XsdAbstractElement> xsdElements, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		List<XsdType> xsdTypes = filterTypes(xsdElements);
		return xsdTypes.stream()
			.map(t -> registerType(t, typeMappings, properties))
			.collect(Collectors.toList());
	}
	public void completeTypes(List<XsdAbstractElement> xsdElements, RosettaXsdMapping typeMappings) {
		List<XsdType> xsdTypes = filterTypes(xsdElements);
		xsdTypes.stream()
			.forEach(t -> completeType(t, typeMappings));
	}
}
