package com.regnosys.rosetta.config.file;

import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public abstract class RosettaGeneratorsConfigurationMixin {
	@JsonCreator
	public RosettaGeneratorsConfigurationMixin(
			@JsonProperty("namespaces") @JsonDeserialize(as=NamespaceFilter.class) Predicate<String> namespaceFilter) {}
}
