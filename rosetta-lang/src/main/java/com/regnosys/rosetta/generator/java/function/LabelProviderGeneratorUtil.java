package com.regnosys.rosetta.generator.java.function;

import javax.inject.Inject;

import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.simple.Function;

public class LabelProviderGeneratorUtil {
	@Inject
	private RosettaFunctionExtensions funcExtensions;
	
	public boolean shouldGenerateLabelProvider(Function function) {
		return !funcExtensions.getTransformAnnotations(function).isEmpty();
	}
}
