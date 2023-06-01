package com.regnosys.rosetta.generator;

import java.lang.reflect.Field;

import org.eclipse.xtext.generator.GeneratorDelegate;

/**
 * Necessary for running the rosetta-maven-plugin.
 *
 */
public class RosettaGeneratorDelegate extends GeneratorDelegate {

	private RosettaGenerator rosettaGenerator = null;

	public RosettaGenerator getRosettaGenerator() {
		if (rosettaGenerator == null) {
			try {
				Field generatorField = GeneratorDelegate.class.getDeclaredField("generator");
				generatorField.setAccessible(true);
				rosettaGenerator = (RosettaGenerator) generatorField.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return rosettaGenerator;
	}
}
