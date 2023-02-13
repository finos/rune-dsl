package com.regnosys.rosetta.ide.semantictokens.lsp;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenModifier;

/**
 * The default semantic token modifiers of the LSP.
 * See https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#semanticTokenModifiers
 * TODO: contribute to Xtext
 */
public enum LSPSemanticTokenModifiersEnum implements ISemanticTokenModifier {
	DECLARATION("declaration"),
	DEFINITION("definition"),
	READONLY("readonly"),
	STATIC("static"),
	DEPRECATED("deprecated"),
	ABSTRACT("abstract"),
	ASYNC("async"),
	MODIFICATION("modification"),
	DOCUMENATION("documentation"),
	DEFAULT_LIBRARY("defaultLibrary");
	
	private final String value;
	
	LSPSemanticTokenModifiersEnum(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return this.value;
	}
}
