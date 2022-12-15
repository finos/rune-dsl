package com.regnosys.rosetta.ide.semantictokens;

import java.util.List;

public interface ISemanticTokenTypesProvider {
	List<ISemanticTokenType> getSemanticTokenTypes();
}
