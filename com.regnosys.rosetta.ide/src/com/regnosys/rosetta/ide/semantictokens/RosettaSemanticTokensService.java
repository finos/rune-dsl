package com.regnosys.rosetta.ide.semantictokens;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.RosettaBuiltinType;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTyped;
import com.regnosys.rosetta.rosetta.simple.Data;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenTypesEnum.*;

public class RosettaSemanticTokensService extends AbstractSemanticTokensService {

	@Inject
	public RosettaSemanticTokensService(ISemanticTokenTypesProvider tokenTypesProvider,
			ISemanticTokenModifiersProvider tokenModifiersProvider) {
		super(tokenTypesProvider, tokenModifiersProvider);
	}

	@MarkSemanticToken
	public SemanticToken markType(RosettaTyped typed) {
		RosettaType t = typed.getType();
		if (t instanceof Data) {
			return createSemanticToken(typed, ROSETTA_TYPED__TYPE, TYPE);
		} else if (t instanceof RosettaBuiltinType) {
			return createSemanticToken(typed, ROSETTA_TYPED__TYPE, BASIC_TYPE);
		} else if (t instanceof RosettaEnumeration) {
			return createSemanticToken(typed, ROSETTA_TYPED__TYPE, ENUM);
		}
		return null;
	}
}
