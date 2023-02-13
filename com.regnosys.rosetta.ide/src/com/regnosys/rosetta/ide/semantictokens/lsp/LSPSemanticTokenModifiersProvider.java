package com.regnosys.rosetta.ide.semantictokens.lsp;

import java.util.Arrays;
import java.util.List;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenModifier;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenModifiersProvider;

/**
 * TODO: contribute to Xtext.
 *
 */
public class LSPSemanticTokenModifiersProvider implements ISemanticTokenModifiersProvider {
	@Override
	public List<ISemanticTokenModifier> getSemanticTokenModifiers() {
		return Arrays.asList(LSPSemanticTokenModifiersEnum.values());
	}
}
