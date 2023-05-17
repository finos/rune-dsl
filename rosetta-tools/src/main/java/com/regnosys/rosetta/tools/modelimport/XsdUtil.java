package com.regnosys.rosetta.tools.modelimport;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
}
