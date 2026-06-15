package com.regnosys.rosetta.config.file;

import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.regnosys.rosetta.config.RuneAttributeReference;

public abstract class RuneGeneratorsConfigurationMixin {
	@JsonCreator
	public RuneGeneratorsConfigurationMixin(
			@JsonProperty("namespaces") @JsonDeserialize(as=NamespaceFilter.class) Predicate<String> namespaceFilter,
			@JsonProperty("doNotPrune") List<RuneAttributeReference> doNotPrune) {}
}
