package com.regnosys.rosetta.tools.modelimport;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaSynonymBody;
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;

public class XsdSynonymImport extends AbstractXsdImport<XsdAbstractElement, RosettaExternalSynonymSource> {

	private final XsdUtil util;
	
	@Inject
	public XsdSynonymImport(XsdUtil util) {
		super(XsdAbstractElement.class);
		this.util = util;
	}

	@Override
	public RosettaExternalSynonymSource registerType(XsdAbstractElement xsdType, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		return null;
	}
	@Override
	public List<? extends RosettaRootElement> registerTypes(List<XsdAbstractElement> xsdElements, RosettaXsdMapping typeMappings, GenerationProperties properties) {
		RosettaExternalSynonymSource synonymSource = RosettaFactory.eINSTANCE.createRosettaExternalSynonymSource();
		synonymSource.setName(properties.getSynonymSourceName());
		typeMappings.registerSynonymSource(synonymSource);
		
		return List.of(synonymSource);
	}

	@Override
	public void completeType(XsdAbstractElement xsdType, RosettaXsdMapping typeMappings) {
		RosettaExternalSynonymSource source = typeMappings.getSynonymSource();
		if (xsdType instanceof XsdComplexType) {
			RosettaExternalClass c = createRosettaExternalClass((XsdComplexType)xsdType, typeMappings);
			source.getExternalClasses().add(c);
		} else if (xsdType instanceof XsdSimpleType && util.isEnumType((XsdSimpleType)xsdType)) {
			RosettaExternalEnum e = createRosettaExternalEnum((XsdSimpleType)xsdType, typeMappings);
			source.getExternalEnums().add(e);
		}
	}
	
	private RosettaExternalClass createRosettaExternalClass(XsdComplexType complexType, RosettaXsdMapping typeMappings) {

		Data data = typeMappings.getRosettaTypeFromComplex(complexType);

		RosettaExternalClass rosettaExternalClass = RosettaFactory.eINSTANCE.createRosettaExternalClass();
		rosettaExternalClass.setData(data);

		Optional.of(complexType)
			.map(XsdComplexType::getElements).stream()
			.flatMap(List::stream)
			.map(ReferenceBase::getElement)
			.filter(XsdElement.class::isInstance)
			.map(XsdElement.class::cast)
			.filter(xsdElement -> xsdElement.getType() != null)
			.forEach(element -> {
				Attribute attr = typeMappings.getAttribute(element);
				
				RosettaExternalRegularAttribute rosettaExternalRegularAttribute = RosettaFactory.eINSTANCE.createRosettaExternalRegularAttribute();
				rosettaExternalRegularAttribute.setAttributeRef(attr);
				rosettaExternalRegularAttribute.setOperator(ExternalValueOperator.PLUS);
				rosettaExternalClass.getRegularAttributes().add(rosettaExternalRegularAttribute);

				RosettaExternalSynonym rosettaExternalSynonym = RosettaFactory.eINSTANCE.createRosettaExternalSynonym();

				RosettaSynonymBody rosettaSynonymBody = RosettaFactory.eINSTANCE.createRosettaSynonymBody();

				RosettaSynonymValueBase rosettaSynonymValueBase = RosettaFactory.eINSTANCE.createRosettaSynonymValueBase();
				rosettaSynonymValueBase.setName(element.getName());

				rosettaSynonymBody.getValues().add(rosettaSynonymValueBase);
				rosettaExternalSynonym.setBody(rosettaSynonymBody);
				rosettaExternalRegularAttribute.getExternalSynonyms().add(rosettaExternalSynonym);
			});

		return rosettaExternalClass;
	}
	
	private RosettaExternalEnum createRosettaExternalEnum(XsdSimpleType simpleType, RosettaXsdMapping typeMappings) {

		RosettaEnumeration enumeration = typeMappings.getRosettaEnumerationFromSimple(simpleType);

		RosettaExternalEnum rosettaExternalEnum = RosettaFactory.eINSTANCE.createRosettaExternalEnum();
		rosettaExternalEnum.setEnumeration(enumeration);

		List<XsdEnumeration> ev = simpleType.getRestriction().getEnumeration();

		ev.stream()
			.forEach(e -> {
				RosettaEnumValue enumValue = typeMappings.getEnumValue(e);
				
				RosettaExternalEnumValue rosettaExternalEnumValue = RosettaFactory.eINSTANCE.createRosettaExternalEnumValue();
				rosettaExternalEnumValue.setEnumRef(enumValue);
				rosettaExternalEnumValue.setOperator(ExternalValueOperator.PLUS);
				rosettaExternalEnum.getRegularValues().add(rosettaExternalEnumValue);

				RosettaEnumSynonym rosettaEnumSynonym = RosettaFactory.eINSTANCE.createRosettaEnumSynonym();
				rosettaEnumSynonym.setSynonymValue(e.getValue());
				rosettaExternalEnumValue.getExternalEnumSynonyms().add(rosettaEnumSynonym);
			});

		return rosettaExternalEnum;
	}
}
