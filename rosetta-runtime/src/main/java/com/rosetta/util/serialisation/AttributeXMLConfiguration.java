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

public class AttributeXMLConfiguration {
	private final Optional<String> xmlName;
	private final Optional<Map<String, String>> xmlAttributes;
	private final Optional<AttributeXMLRepresentation> xmlRepresentation;
	private final Optional<String> substitutionGroup;
	
	@JsonCreator
	public AttributeXMLConfiguration(
			@JsonProperty("xmlName") Optional<String> xmlName,
			@JsonProperty("xmlAttributes") Optional<Map<String, String>> xmlAttributes,
			@JsonProperty("xmlRepresentation") Optional<AttributeXMLRepresentation> xmlRepresentation,
			@JsonProperty("substitutionGroup") Optional<String> substitutionGroup) {
		this.xmlName = xmlName;
		this.xmlAttributes = xmlAttributes;
		this.xmlRepresentation = xmlRepresentation;
		this.substitutionGroup = substitutionGroup;
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
	
	public Optional<String> getSubstitutionGroup() {
		return substitutionGroup;
	}

	@Override
	public int hashCode() {
		return Objects.hash(xmlAttributes, xmlName, xmlRepresentation, substitutionGroup);
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
				&& Objects.equals(xmlName, other.xmlName) && Objects.equals(xmlRepresentation, other.xmlRepresentation)
				&& Objects.equals(substitutionGroup, other.substitutionGroup);
	}
}
