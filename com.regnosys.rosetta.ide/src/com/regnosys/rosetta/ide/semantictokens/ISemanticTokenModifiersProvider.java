package com.regnosys.rosetta.ide.semantictokens;

import java.util.List;

public interface ISemanticTokenModifiersProvider {
	List<ISemanticTokenModifier> getSemanticTokenModifiers();
}
