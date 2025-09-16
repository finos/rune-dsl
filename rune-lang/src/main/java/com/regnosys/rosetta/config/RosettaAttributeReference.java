package com.regnosys.rosetta.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RosettaAttributeReference {
	private final String type;
	private final String attribute;
	
	@JsonCreator
	public RosettaAttributeReference(
			@JsonProperty("type") String type,
			@JsonProperty("attribute") String attribute) {
		this.type = type;
		this.attribute = attribute;
	}
	
	public String getType() {
		return type;
	}
	public String getAttribute() {
		return attribute;
	}
}
