package com.regnosys.rosetta.config.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class JavaConfigurationMixin {
	@JsonCreator
	public JavaConfigurationMixin(
			@JsonProperty("runtimeModuleClass") String runtimeModuleClass) {}
}
