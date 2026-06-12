package com.regnosys.rosetta.codegen.api;

/**
 * Represents a fragment of generated target-language code.
 */
public interface TargetLanguageRepresentation extends CodeRenderer {
	@Override
	void render(CodeWriter out);
}
