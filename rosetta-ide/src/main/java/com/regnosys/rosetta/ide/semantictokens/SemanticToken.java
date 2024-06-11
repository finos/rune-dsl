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

package com.regnosys.rosetta.ide.semantictokens;

/**
 * TODO: contribute to Xtext.
 *
 */
public class SemanticToken implements Comparable<SemanticToken> {
	private final int line;
	private final int startChar;
	private final int length;
	private final ISemanticTokenType tokenType;
	private final ISemanticTokenModifier[] tokenModifiers;
	
	public SemanticToken(
			int line,
			int startChar,
			int length,
			ISemanticTokenType tokenType,
			ISemanticTokenModifier[] tokenModifiers
	) {
		this.line = line;
		this.startChar = startChar;
		this.length = length;
		this.tokenType = tokenType;
		this.tokenModifiers = tokenModifiers;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public int getStartChar() {
		return this.startChar;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public ISemanticTokenType getTokenType() {
		return this.tokenType;
	}
	
	public ISemanticTokenModifier[] getTokenModifiers() {
		return this.tokenModifiers;
	}

	@Override
	public int compareTo(SemanticToken o) {
		if (this.line < o.line) {
			return -1;
		}
		if (this.line > o.line) {
			return 1;
		}
		return this.startChar - o.startChar;
	}
}
