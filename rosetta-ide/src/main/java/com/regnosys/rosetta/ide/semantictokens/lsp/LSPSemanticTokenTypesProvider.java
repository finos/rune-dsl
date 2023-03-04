package com.regnosys.rosetta.ide.semantictokens.lsp;

import java.util.Arrays;
import java.util.List;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenType;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenTypesProvider;

/**
 * TODO: contribute to Xtext.
 *
 */
public class LSPSemanticTokenTypesProvider implements ISemanticTokenTypesProvider {
	@Override
	public List<ISemanticTokenType> getSemanticTokenTypes() {
		return Arrays.asList(LSPSemanticTokenTypesEnum.values());
	}
}
