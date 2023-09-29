package com.rosetta.util.serialisation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeXMLConfiguration {
	private final Optional<String> xmlRootElementName;
	private final Optional<Map<String, String>> xmlAttributes;
	private final Optional<Map<String, AttributeXMLConfiguration>> attributes;
	
	@JsonCreator
	public TypeXMLConfiguration(
			@JsonProperty("xmlRootElementName") Optional<String> xmlRootElementName,
			@JsonProperty("xmlAttributes") Optional<Map<String, String>> xmlAttributes,
			@JsonProperty("attributes") Optional<Map<String, AttributeXMLConfiguration>> attributes) {
		this.xmlRootElementName = xmlRootElementName;
		this.xmlAttributes = xmlAttributes;
		this.attributes = attributes;
	}

	public Optional<String> getXmlRootElementName() {
		return xmlRootElementName;
	}

	public Optional<Map<String, String>> getXmlAttributes() {
		return xmlAttributes;
	}

	public Optional<Map<String, AttributeXMLConfiguration>> getAttributes() {
		return attributes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, xmlAttributes, xmlRootElementName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeXMLConfiguration other = (TypeXMLConfiguration) obj;
		return Objects.equals(attributes, other.attributes)
				&& Objects.equals(xmlAttributes, other.xmlAttributes)
				&& Objects.equals(xmlRootElementName, other.xmlRootElementName);
	}
}
