package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.rosetta.simple.Function;

public class LabelProviderGeneratorUtil {

	public boolean shouldGenerateLabelProvider(Function function) {
		return !function.getTransform().isEmpty();
	}
}
