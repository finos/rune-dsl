/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	public RosettaEnumeration registerType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, ImportTargetConfig targetConfig) {
		RosettaEnumeration rosettaEnumeration = RosettaFactory.eINSTANCE.createRosettaEnumeration();
		rosettaEnumeration.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(rosettaEnumeration::setDefinition);
		typeMappings.registerEnumType(xsdType, rosettaEnumeration);
		
		List<XsdEnumeration> enumeration = xsdType.getAllRestrictions().stream().flatMap(r -> r.getEnumeration().stream()).toList();

		enumeration.stream()
			.filter(e -> !e.getValue().isEmpty())
			.map(e -> this.registerEnumValue(e, typeMappings))
			.forEach(rosettaEnumeration.getEnumValues()::add);
		
		return rosettaEnumeration;
	}

	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
		
	}

	private RosettaEnumValue registerEnumValue(XsdEnumeration ev, RosettaXsdMapping typeMappings) {
		String value = util.toEnumValueName(ev.getValue());
		RosettaEnumValue rosettaEnumValue = RosettaFactory.eINSTANCE.createRosettaEnumValue();
		rosettaEnumValue.setName(value);
		util.extractDocs(ev).ifPresent(rosettaEnumValue::setDefinition);

		typeMappings.registerEnumValue(ev, rosettaEnumValue);
		
		return rosettaEnumValue;
	}
}
