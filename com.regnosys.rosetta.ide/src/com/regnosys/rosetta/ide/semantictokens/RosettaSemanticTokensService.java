package com.regnosys.rosetta.ide.semantictokens;

import javax.inject.Inject;

//import com.regnosys.rosetta.rosetta.RosettaSymbol;
//import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
//
//import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
//import static com.regnosys.rosetta.ide.semantictokens.lsp.LSPSemanticTokenTypesEnum.*;

public class RosettaSemanticTokensService extends AbstractSemanticTokensService {

	@Inject
	public RosettaSemanticTokensService(ISemanticTokenTypesProvider tokenTypesProvider,
			ISemanticTokenModifiersProvider tokenModifiersProvider) {
		super(tokenTypesProvider, tokenModifiersProvider);
	}
// Example:	
//	@MarkSemanticToken
//	public SemanticToken markSymbolReference(RosettaSymbolReference ref) {
//		RosettaSymbol symbol = ref.getSymbol();
//		if (symbol.eContainmentFeature().equals(FUNCTION__INPUTS)) {
//			return createSemanticToken(ref, PARAMETER);
//		}
//		return null;
//	}
}
