package com.regnosys.rosetta.ide.semantictokens;

import java.util.Arrays;
import java.util.List;

public class RosettaSemanticTokenTypesProvider  implements ISemanticTokenTypesProvider {
	@Override
	public List<ISemanticTokenType> getSemanticTokenTypes() {
		return Arrays.asList(RosettaSemanticTokenTypesEnum.values());
	}
}
