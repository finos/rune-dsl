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

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.RosettaNamed;

import org.xmlet.xsdparser.core.XsdParserCore;
import org.xmlet.xsdparser.xsdelements.*;

public class XsdUtil {
	private final Set<String> documentationSources = Set.of("Definition");
	
	public final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				// default to definition if source not specified
				.filter(x -> x.getSource() == null || documentationSources.contains(x.getSource()))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace("\r\n", " "))
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			)
			.map(docs -> docs.isEmpty() ? null : docs);
	}
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev, String docAnnotationSourceName) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				.filter(x -> x.getSource() != null)
				.filter(x -> x.getSource().equals(docAnnotationSourceName))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace("\r\n", " "))
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			)
			.map(docs -> docs.isEmpty() ? null : docs);
	}
	
	public boolean isEnumType(XsdSimpleType simpleType) {
		return getRestrictions(simpleType).stream()
				.anyMatch(e -> !e.getEnumeration().isEmpty());
	}

	public List<XsdRestriction> getRestrictions(XsdSimpleType simpleType) {
		Map<String, XsdRestriction> restrictions = new HashMap<>();
		List<XsdRestriction> result = new ArrayList<>();

		XsdRestriction restriction = simpleType.getRestriction();
		XsdUnion union = simpleType.getUnion();

		if (restriction != null){
			result.add(restriction);
		}

		if (union != null){
			result.addAll(union.getUnionElements().stream().map(XsdSimpleType::getRestriction).toList());
		}
		return result;
	}
	
	public String getQualifiedName(XsdNamedElements elem) {
		String name = elem.getName();
		
		XsdAbstractElement original = getOriginalElement(elem);
		original.setParentAvailable(true);
		XsdSchema schema = original.getXsdSchema();
		if (schema == null) {
			return name;
		}
		
		String targetNamespace = schema.getTargetNamespace();
		if (targetNamespace == null) {
			return name;
		}
		return targetNamespace + "/" + name;
	}
	
	public boolean isTopLevelElement(XsdAbstractElement elem) {
		XsdAbstractElement original = getOriginalElement(elem);
		original.setParentAvailable(true);
		XsdAbstractElement p = original.getParent();
		return p instanceof XsdSchema;
	}
	private XsdAbstractElement getOriginalElement(XsdAbstractElement elem) {
		XsdAbstractElement original = elem;
		while (original.getCloneOf() != null) {
			original = original.getCloneOf();
		}
		return original;
	}

    public String toTypeName(String xsdName, ImportTargetConfig config) {
    	String overridenName = config.getNameOverrides().get(xsdName);
    	if (overridenName != null) {
    		return overridenName;
    	}
        String name = config.getPreferences().getTypeCasing().transform(xsdName);
        // TODO
        if (name.equals("Object")) {
        	return "_Object";
        }
        return name;
    }

    public String toAttributeName(String xsdName, ImportTargetConfig config) {
        return config.getPreferences().getAttributeCasing().transform(xsdName);
    }
    
    public String toEnumValueName(String xsdName, ImportTargetConfig config) {
    	return config.getPreferences().getEnumValueCasing().transform(xsdName);
    }

	public void makeNamesUnique(List<? extends RosettaNamed> objects) {
		objects.stream().collect(Collectors.groupingBy(RosettaNamed::getName)).forEach((name, group) -> {
			if (group.size() > 1) {
				for (int i=0; i<group.size(); i++) {
					group.get(i).setName(name + i);
				}
			}
		});
	}
}
