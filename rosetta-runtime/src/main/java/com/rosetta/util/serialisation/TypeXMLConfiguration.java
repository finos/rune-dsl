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

package com.rosetta.util.serialisation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rosetta.model.lib.ModelSymbolId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TypeXMLConfiguration {
	@Deprecated
	private final Optional<ModelSymbolId> substitutionFor; // replaced by substitutionGroup
	@Deprecated
	private final Optional<String> substitutionGroup;
	private final Optional<List<XmlElement>> elements;
	@Deprecated
	private final Optional<String> xmlElementName;
	private final Optional<Map<String, String>> xmlAttributes;
	private final Optional<Map<String, AttributeXMLConfiguration>> attributes;
	private final Optional<Map<String, String>> enumValues;

	@JsonCreator
	public TypeXMLConfiguration(
            @JsonProperty("substitutionFor") @Deprecated Optional<ModelSymbolId> substitutionFor,
            @JsonProperty("substitutionGroup") @Deprecated Optional<String> substitutionGroup,
			@JsonProperty("elements") Optional<List<XmlElement>> elements,
            @JsonProperty("xmlElementName") @Deprecated Optional<String> xmlElementName,
            @JsonProperty("xmlAttributes") Optional<Map<String, String>> xmlAttributes,
            @JsonProperty("attributes") Optional<Map<String, AttributeXMLConfiguration>> attributes,
            @JsonProperty("enumValues") Optional<Map<String, String>> enumValues) {
		this.substitutionFor = substitutionFor;
		this.substitutionGroup = substitutionGroup;
        this.elements = elements;
        this.xmlElementName = xmlElementName;
		this.xmlAttributes = xmlAttributes;
		this.attributes = attributes;
		this.enumValues = enumValues;
	}
	
	public TypeXMLConfiguration(
			@Deprecated Optional<String> substitutionGroup,
			Optional<List<XmlElement>> elements,
			@Deprecated Optional<String> xmlElementName,
            Optional<Map<String, String>> xmlAttributes,
            Optional<Map<String, AttributeXMLConfiguration>> attributes,
            Optional<Map<String, String>> enumValues) {
		this(Optional.empty(), substitutionGroup, elements, xmlElementName, xmlAttributes, attributes, enumValues);
	}
	
	@Deprecated // Use getSubstitutionGroup instead
	public Optional<ModelSymbolId> getSubstitutionFor() {
		return substitutionFor;
	}

	@Deprecated
	public Optional<String> getSubstitutionGroup() {
		return substitutionGroup;
	}

	@Deprecated
	public Optional<String> getXmlElementName() {
		return xmlElementName;
	}

	public Optional<Map<String, String>> getXmlAttributes() {
		return xmlAttributes;
	}

	public Optional<Map<String, AttributeXMLConfiguration>> getAttributes() {
		return attributes;
	}
	
	public Optional<Map<String, String>> getEnumValues() {
		return enumValues;
	}

	public Optional<List<XmlElement>> getElements() {
		return elements;
	}

	@Override
	public int hashCode() {
		return Objects.hash(substitutionGroup, elements, xmlAttributes, xmlElementName, attributes, enumValues);
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
		return Objects.equals(substitutionGroup, other.substitutionGroup)
				&& Objects.equals(elements, other.elements)
				&& Objects.equals(xmlAttributes, other.xmlAttributes)
				&& Objects.equals(xmlElementName, other.xmlElementName)
				&& Objects.equals(attributes, other.attributes)
				&& Objects.equals(enumValues, other.enumValues);
	}
}
