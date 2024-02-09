package com.regnosys.rosetta.ide.semantictokens;

import java.util.Arrays;
import java.util.List;

public class RosettaSemanticTokenModifiersProvider  implements ISemanticTokenModifiersProvider {
	@Override
	public List<ISemanticTokenModifier> getSemanticTokenModifiers() {
		return Arrays.asList(RosettaSemanticTokenModifiersEnum.values());
	}
}
