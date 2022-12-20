package com.regnosys.rosetta.ide.semantictokens.lsp;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenType;

/**
 * The default semantic token types of the LSP.
 * See https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#semanticTokenTypes
 */
public enum LSPSemanticTokenTypesEnum implements ISemanticTokenType {
	NAMESPACE("namespace"),
	TYPE("type"),
	CLASS("class"),
	ENUM("enum"),
	INTERFACE("interface"),
	STRUCT("struct"),
	TYPE_PARAMETER("typeParameter"),
	PARAMETER("parameter"),
	VARIABLE("variable"),
	PROPERTY("property"),
	ENUM_MEMBER("enumMember"),
	EVENT("event"),
	FUNCTION("function"),
	METHOD("method"),
	MACRO("macro"),
	KEYWORD("keyword"),
	MODIFIER("modifier"),
	COMMENT("comment"),
	STRING("string"),
	NUMBER("number"),
	REGEXP("regexp"),
	OPERATOR("operator"),
	DECORATOR("decorator");

	private final String value;
	
	LSPSemanticTokenTypesEnum(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return this.value;
	}
}
