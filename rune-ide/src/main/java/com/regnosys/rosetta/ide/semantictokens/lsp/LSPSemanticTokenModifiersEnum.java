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
