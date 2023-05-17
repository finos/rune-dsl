package com.regnosys.rosetta.tools.modelimport;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.xmlet.xsdparser.xsdelements.XsdAnnotatedElements;
import org.xmlet.xsdparser.xsdelements.XsdAnnotation;
import org.xmlet.xsdparser.xsdelements.XsdAnnotationChildren;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

public class XsdUtil {
	private final Set<String> documentationSources = Set.of("Definition");
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				// default to definition if source not specified
				.filter(x -> x.getSource() == null || documentationSources.contains(x.getSource()))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			);
	}
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev, String docAnnotationSourceName) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				.filter(x -> x.getSource() != null)
				.filter(x -> x.getSource().equals(docAnnotationSourceName))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			);
	}
	
	public boolean isEnumType(XsdSimpleType simpleType) {
		return simpleType.getAllRestrictions().stream()
				.anyMatch(e -> e.getEnumeration().size() > 0);
	}
	
	public String allFirstLowerIfNotAbbrevation(String s) {
		if (s == null || s.length() == 0)
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
