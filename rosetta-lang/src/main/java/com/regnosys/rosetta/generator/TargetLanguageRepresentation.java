package com.regnosys.rosetta.generator;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

/**
 * An interface to indicate that a class represents a concept in a target language
 * of a generator.
 *
 * When appended to a {com.regnosys.rosetta.generator.TargetLanguageStringConcatenation},
 * `appendTo` will be called instead of `toString`.
 */
public interface TargetLanguageRepresentation {
	void appendTo(TargetStringConcatenation target);
}
