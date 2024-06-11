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

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.xmlet.xsdparser.xsdelements.XsdAnnotatedElements;
import org.xmlet.xsdparser.xsdelements.XsdAnnotation;
import org.xmlet.xsdparser.xsdelements.XsdAnnotationChildren;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

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
		return simpleType.getAllRestrictions().stream()
				.anyMatch(e -> !e.getEnumeration().isEmpty());
	}

    public String toTypeName(String xsdName) {
        String[] parts = xsdName.split("[^a-zA-Z0-9]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (Character.isUpperCase(part.charAt(0))) {
                builder.append(part);
            } else {
                builder.append(Character.toUpperCase(part.charAt(0)));
                builder.append(part, 1, part.length());
            }
        }
        return builder.toString();
    }

    public String toAttributeName(String xsdName) {
        String[] parts = xsdName.split("[^a-zA-Z0-9]");
        StringBuilder builder = new StringBuilder();
        builder.append(allFirstLowerIfNotAbbrevation(parts[0]));
        Arrays.stream(parts).skip(1).forEach(part -> {
            if (Character.isUpperCase(part.charAt(0))) {
                builder.append(part);
            } else {
                builder.append(Character.toUpperCase(part.charAt(0)));
                builder.append(part, 1, part.length());
            }
        });
        return builder.toString();
    }
	
	private String allFirstLowerIfNotAbbrevation(String s) {
		if (s == null || s.isEmpty())
			return s;
		int upperCased = 0;
		while (upperCased < s.length() && Character.isUpperCase(s.charAt(upperCased))) {
			upperCased++;
		}
		if (upperCased == 0)
			return s;
		if (s.length() == upperCased)
			return s.toLowerCase();
		if (upperCased == 1) {
			return s.substring(0, 1).toLowerCase() + s.substring(1);
		}
		return s.substring(0, upperCased - 1).toLowerCase() + s.substring(upperCased - 1);
	}
}
