package com.regnosys.rosetta.formatting2;

/**
 * NORMAL: put on multiple lines if it's too long, otherwise on a single line.
 * SINGLE_LINE: put everything on a single line.
 * CHAIN: put everything on newlines until the chain stops. What a "chain" is, is
 *        left up to the implementation of the formatter.
 */
public enum FormattingMode {
	NORMAL,
	SINGLE_LINE,
	CHAIN;
	
	public FormattingMode stopChain() {
		if (this.equals(FormattingMode.CHAIN)) {
			return FormattingMode.NORMAL;
		}
		return this;
	}
	
	public FormattingMode chainIf(boolean condition) {
		if (this.equals(FormattingMode.SINGLE_LINE)) {
			return this;
		}
		if (condition) {
			return FormattingMode.CHAIN;
		}
		return FormattingMode.NORMAL;
	}
}
