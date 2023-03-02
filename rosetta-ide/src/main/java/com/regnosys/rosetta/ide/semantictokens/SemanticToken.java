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
