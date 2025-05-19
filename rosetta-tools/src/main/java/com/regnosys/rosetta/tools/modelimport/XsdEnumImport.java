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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

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
			.map(e -> this.registerEnumValue(e, typeMappings, targetConfig))
			.forEach(rosettaEnumeration.getEnumValues()::add);
		
		return rosettaEnumeration;
	}

	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
		
	}

	private RosettaEnumValue registerEnumValue(XsdEnumeration ev, RosettaXsdMapping typeMappings, ImportTargetConfig targetConfig) {
		String value = util.toEnumValueName(ev.getValue(), targetConfig);
		RosettaEnumValue rosettaEnumValue = RosettaFactory.eINSTANCE.createRosettaEnumValue();
		rosettaEnumValue.setName(value);
		util.extractDocs(ev).ifPresent(rosettaEnumValue::setDefinition);

		typeMappings.registerEnumValue(ev, rosettaEnumValue);
		
		return rosettaEnumValue;
	}
	
	public Map<RosettaEnumeration, TypeXMLConfiguration> getXMLConfiguration(XsdSimpleType xsdType, RosettaXsdMapping xsdMapping, String schemaTargetNamespace) {		
		Map<String, String> enumValueMap = new LinkedHashMap<>();
		xsdType.getAllRestrictions().stream().flatMap(r -> r.getEnumeration().stream())
			.forEach(ev -> {
				RosettaEnumValue rosettaEnumValue = xsdMapping.getEnumValue(ev);
				if (!rosettaEnumValue.getName().equals(ev.getValue())) {
					enumValueMap.put(rosettaEnumValue.getName(), ev.getValue());
				}
			});
		if (enumValueMap.isEmpty()) {
			return Collections.emptyMap();
		}
		RosettaEnumeration rosettaEnumeration = xsdMapping.getRosettaEnumerationFromSimple(xsdType);
		return Collections.singletonMap(rosettaEnumeration, new TypeXMLConfiguration(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.of(enumValueMap)));
	}
}
