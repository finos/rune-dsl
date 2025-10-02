/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.semantictokens.lsp;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenType;

/**
 * The default semantic token types of the LSP.
 * See https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#semanticTokenTypes
 * TODO: contribute to Xtext
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
