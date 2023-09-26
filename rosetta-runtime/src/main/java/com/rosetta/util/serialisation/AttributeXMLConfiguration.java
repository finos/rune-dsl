package com.rosetta.util.serialisation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AttributeXMLConfiguration {
	private final Optional<String> xmlName;
	private final Optional<Map<String, String>> xmlAttributes;
	private final Optional<AttributeXMLRepresentation> xmlRepresentation;
	
	@JsonCreator
	public AttributeXMLConfiguration(
			@JsonProperty("xmlName") Optional<String> xmlName,
			@JsonProperty("xmlAttributes") Optional<Map<String, String>> xmlAttributes,
			@JsonProperty("xmlRepresentation") Optional<AttributeXMLRepresentation> xmlRepresentation) {
		this.xmlName = xmlName;
		this.xmlAttributes = xmlAttributes;
		this.xmlRepresentation = xmlRepresentation;
	}

	public Optional<String> getXmlName() {
		return xmlName;
	}

	public Optional<Map<String, String>> getXmlAttributes() {
		return xmlAttributes;
	}

	public Optional<AttributeXMLRepresentation> getXmlRepresentation() {
		return xmlRepresentation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(xmlAttributes, xmlName, xmlRepresentation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeXMLConfiguration other = (AttributeXMLConfiguration) obj;
		return Objects.equals(xmlAttributes, other.xmlAttributes)
				&& Objects.equals(xmlName, other.xmlName) && Objects.equals(xmlRepresentation, other.xmlRepresentation);
	}
}
