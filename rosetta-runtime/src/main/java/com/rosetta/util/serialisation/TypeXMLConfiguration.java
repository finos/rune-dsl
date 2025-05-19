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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rosetta.model.lib.ModelSymbolId;

public class TypeXMLConfiguration {
	@Deprecated
	private final Optional<ModelSymbolId> substitutionFor; // replaced by substitutionGroup
	private final Optional<String> substitutionGroup;
	private final Optional<String> xmlElementName;
	private final Optional<String> xmlElementFullyQualifiedName;
	private final Optional<Boolean> isAbstract;
	private final Optional<Map<String, String>> xmlAttributes;
	private final Optional<Map<String, AttributeXMLConfiguration>> attributes;
	private final Optional<Map<String, String>> enumValues;

	@JsonCreator
	public TypeXMLConfiguration(
            @JsonProperty("substitutionFor") @Deprecated Optional<ModelSymbolId> substitutionFor,
            @JsonProperty("substitutionGroup") Optional<String> substitutionGroup,
            @JsonProperty("xmlElementName") Optional<String> xmlElementName,
            @JsonProperty("xmlElementFullyQualifiedName") Optional<String> xmlElementFullyQualifiedName,
			@JsonProperty("abstract") Optional<Boolean> isAbstract,
            @JsonProperty("xmlAttributes") Optional<Map<String, String>> xmlAttributes,
            @JsonProperty("attributes") Optional<Map<String, AttributeXMLConfiguration>> attributes,
            @JsonProperty("enumValues") Optional<Map<String, String>> enumValues) {
		this.substitutionFor = substitutionFor;
		this.substitutionGroup = substitutionGroup;
        this.xmlElementName = xmlElementName;
        this.xmlElementFullyQualifiedName = xmlElementFullyQualifiedName;
        this.isAbstract = isAbstract;
        this.xmlAttributes = xmlAttributes;
		this.attributes = attributes;
		this.enumValues = enumValues;
	}
	
	public TypeXMLConfiguration(
			Optional<String> substitutionGroup,
			Optional<String> xmlElementName,
			Optional<String> xmlElementFullyQualifiedName,
			Optional<Boolean> isAbstract,
			Optional<Map<String, String>> xmlAttributes,
			Optional<Map<String, AttributeXMLConfiguration>> attributes,
			Optional<Map<String, String>> enumValues) {
		this(Optional.empty(), substitutionGroup, xmlElementName, xmlElementFullyQualifiedName, isAbstract, xmlAttributes, attributes, enumValues);
	}
	
	@Deprecated // Use getSubstitutionGroup instead
	public Optional<ModelSymbolId> getSubstitutionFor() {
		return substitutionFor;
	}

	public Optional<String> getSubstitutionGroup() {
		return substitutionGroup;
	}

	public Optional<String> getXmlElementName() {
		return xmlElementName;
	}

	public Optional<String> getXmlElementFullyQualifiedName() {
		return xmlElementFullyQualifiedName;
	}

	public Optional<Boolean> getAbstract() {
		return isAbstract;
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

	@Override
	public int hashCode() {
		return Objects.hash(substitutionGroup, xmlAttributes, xmlElementName, xmlElementFullyQualifiedName, isAbstract, attributes, enumValues);
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
				&& Objects.equals(xmlAttributes, other.xmlAttributes)
				&& Objects.equals(xmlElementName, other.xmlElementName)
				&& Objects.equals(xmlElementFullyQualifiedName, other.xmlElementFullyQualifiedName)
				&& Objects.equals(isAbstract, other.isAbstract)
				&& Objects.equals(attributes, other.attributes)
				&& Objects.equals(enumValues, other.enumValues);
	}
}
