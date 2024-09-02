/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package com.regnosys.rosetta.xcore.compressor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the typical if-else-cascaded created by the ANTLR parser and which may lead to methods too long for beeing
 * compilable. The parser is not robust, e.g., it does not perform checks on end of file. It assumes the parsed java
 * code to be an ANTLR parser.
 *
 * It works closely together with the {@link ParserCompressorFragment2}. The cascades will look like that:
 *
 * <pre>
 * ...
 * else if ( (LA450_383==Solidus) && (synpred265_InternalN4JSParser())) {s = 282;}
 * else if ( (LA450_383==PercentSign) && (synpred265_InternalN4JSParser())) {s = 283;}
 * else if ( (LA450_383==PlusSign) && (synpred265_InternalN4JSParser())) {s = 284;}
 * ...
 * </pre>
 */
class IfElseCascadeParser {

	private final String grammarContent;
	private int offset;
	/**
	 * Set to null before each {@link #findCascades(int)} call, will only be set to a map when at least the first if
	 * statement is found.
	 */
	private Map<String, Integer> tokenToStateMap;

	private String tokenVarName;
	private String condition;
	private boolean bElse;

	private List<IfElseCascade> cascades;
	private int start;

	public IfElseCascadeParser(String grammarContent) {
		this.grammarContent = grammarContent;
	}

	List<IfElseCascade> findCascades(int i_offset) {
		cascades = new ArrayList<>();
		tokenToStateMap = null;
		this.offset = i_offset;
		this.start = offset;
		skipWS();
		if (!firstIf()) {
			return null;
		}
		int oldOffset;
		do {
			oldOffset = offset;
		} while (nextIf());
		offset = oldOffset;
		addCascade(start, offset);
		return cascades;

	}

	private void addCascade(int cStart, int cEnd) {
		if (tokenToStateMap != null && tokenToStateMap.size() > 1) {
			IfElseCascade cascade = new IfElseCascade(cStart, cEnd, bElse, tokenVarName, condition, tokenToStateMap);
			cascades.add(cascade);
			tokenToStateMap = new HashMap<>();
		}

	}

	private boolean firstIf() {
		bElse = next("else");
		if (!next("if", "(", "(")) {
			return false;
		}
		tokenVarName = findVar();
		if (tokenVarName.isEmpty()) {
			return false;
		}
		if (!next("==")) {
			return false;
		}
		String tokenName = findVar();
		if (tokenName.isEmpty()) {
			return false;
		}
		if (!next(")")) {
			return false;
		}
		condition = findCondition();
		if (!next(")", "{", "s", "=")) {
			return false;
		}
		Integer stateValue = findIntValue();
		if (stateValue == null) {
			return false;
		}
		tokenToStateMap = new HashMap<>();
		tokenToStateMap.put(tokenName, stateValue);
		return next(";", "}");
	}

	private boolean nextIf() {
		int ifStart = offset;
		if (!next("else", "if", "(", "(", tokenVarName, "==")) {
			return false;
		}
		String tokenName = findVar();
		if (tokenName.isEmpty()) {
			return false;
		}
		if (!next(")")) {
			return false;
		}
		String nextCondition = findCondition();
		if (!condition.equals(nextCondition)) {
			addCascade(start, ifStart);
			condition = nextCondition;
			start = ifStart;
			bElse = true;
		}
		if (!next(")", "{", "s", "=")) {
			return false;
		}
		Integer stateValue = findIntValue();
		if (stateValue == null) {
			return false;
		}
		tokenToStateMap.put(tokenName, stateValue);
		return next(";", "}");
	}

	private String findCondition() {
		int o = offset;
		int parenthesisCount = 0;
		do {
			char c = grammarContent.charAt(o);
			switch (c) {
			case '(':
				parenthesisCount++;
				break;
			case ')':
				parenthesisCount--;
				if (parenthesisCount < 0) {
					String cond = grammarContent.substring(offset, o);
					offset = o;
					return cond.trim();
				}
			}
			o++;
		} while (true);
	}

	private String findVar() {
		int o = offset;
		while (Character.isJavaIdentifierPart(grammarContent.charAt(o))) {
			o++;
		}
		String varName = grammarContent.substring(offset, o);
		offset = o;
		return varName;
	}

	private Integer findIntValue() {
		int o = offset;
		while (Character.isDigit(grammarContent.charAt(o))) {
			o++;
		}
		if (o == offset) {
			return null;
		}
		String value = grammarContent.substring(offset, o);
		offset = o;
		return Integer.parseInt(value);
	}

	private void skipWS() {
		while (Character.isWhitespace(grammarContent.charAt(offset))) {
			offset++;
		}
	}

	private boolean next(String... tokens) {
		int oldOffset = offset;
		for (String token : tokens) {
			if (!token.isEmpty()) {
				if (grammarContent.startsWith(token, offset)) {
					offset += token.length();
					skipWS();
				} else {
					offset = oldOffset;
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Only for debugging.
	 */
	@Override
	public String toString() {
		int from = offset - 10;
		int to = offset + 10;
		if (from < 0)
			from = 0;
		if (to > grammarContent.length())
			to = grammarContent.length();
		StringBuilder strb = new StringBuilder();
		for (int i = from; i < to; i++) {
			char c = grammarContent.charAt(i);
			if (i == offset) {
				strb.append("«");
			}
			if (c == '\n') {
				strb.append("\\n");
			} else if (c == '\r') {
				strb.append("\\r");
			} else if (c == '\t') {
				strb.append("\\t");
			} else {
				strb.append(c);
			}
			if (i == offset) {
				strb.append("»");
			}
		}
		return strb.toString();
	}

}