package com.regnosys.rosetta.generator;

import javax.inject.Inject;

import org.eclipse.xtext.generator.GeneratorDelegate;

public class RosettaGeneratorDelegate extends GeneratorDelegate {

	private final RosettaGenerator rosettaGenerator;
	
	@Inject
	public RosettaGeneratorDelegate(RosettaGenerator rosettaGenerator) {
		this.rosettaGenerator = rosettaGenerator;
	}

	public RosettaGenerator getRosettaGenerator() {
		return rosettaGenerator;
	}
}
