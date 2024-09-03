package com.regnosys.rosetta.config.file;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RosettaTabulatorConfigurationMixin {
	@JsonCreator
	public RosettaTabulatorConfigurationMixin(@JsonProperty("annotations") List<String> annotations,
			@JsonProperty("types") List<String> types) {}

}
