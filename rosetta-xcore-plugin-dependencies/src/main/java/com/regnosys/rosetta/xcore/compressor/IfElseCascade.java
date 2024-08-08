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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores information about a compressible if-else-cascade, including operations to emit replacement strings.
 */
public class IfElseCascade {

	/**
	 * Value object with the concrete replacements to be inserted into the parser instead of the cascade.
	 */
	public class Replacement {
		private final int min;
		private final int max;
		private final int id;

		/**
		 * The array literal with the matrix definition. Just the literal, e.g. <code>{...}</code>
		 */
		public final String arrayLiteral;

		Replacement(int id, String arrayLiteral, int min, int max) {
			this.arrayLiteral = arrayLiteral;
			this.min = min;
			this.max = max;
			this.id = id;
		}

		String getMatrixDefinition() {
			return "final static int[] " + IfElseCascade.getMatrixNameSimple(id) + " = " + arrayLiteral + ";";
		}

		String getStatement() {
			return getStatement(null);
		}

		String getStatement(String delegate) {
			String matrixName = delegate == null ? IfElseCascade.getMatrixName(id) : delegate;
			String stmt = (bElse ? "else " : "") +
					"if ((" + tokenVarName + ">=" + min + " && " +
					tokenVarName + "<=" + max + " && (s=" + matrixName + "[" + tokenVarName
					+ "-(" + min + ")]" + ")>=0)" +
					(condition.isEmpty() ? "" : " " + condition)
					+ ") { /* " + tokenToStateMap.size() + " cases */ }";
			return stmt;
		}

	}

	final static String MATRIX_CLASS = "T2S";
	final static String MATRIX_PREFIX = "M_";

	final int start, end;
	final boolean bElse;
	final String tokenVarName;
	final String condition;
	final Map<String, Integer> tokenToStateMap;

	/**
	 * If-Else-Cascade that can be replaced with a transformation matrix call.
	 */
	public IfElseCascade(int start, int end, boolean bElse, String tokenVarName, String condition,
			Map<String, Integer> tokenToStateMap) {
		this.start = start;
		this.end = end;
		this.bElse = bElse;
		this.tokenVarName = tokenVarName;
		this.condition = condition;
		this.tokenToStateMap = tokenToStateMap;
	}

	/**
	 * Returns two strings, first one is the transformation matrix definition and the second one the statement which
	 * replaces the cascade.
	 */
	public Replacement getReplacements(Map<String, Integer> tokensToValues, int id) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Map<Integer, Integer> valueToState = new HashMap<>();
		for (Entry<String, Integer> t2s : tokenToStateMap.entrySet()) {
			String token = t2s.getKey();
			int state = t2s.getValue();
			Integer iValue = tokensToValues.get(token);
			if (iValue == null) {
				iValue = tokensToValues.get("T__" + token);
				if (iValue == null) {
					throw new IllegalStateException("No constant value of " + token + " found in grammar file.");
				}
			}
			int value = iValue.intValue();
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
			valueToState.put(value, state);
		}

		String arrayLiteral = createArrayLiteral(min, max, valueToState);
		return new Replacement(id, arrayLiteral, min, max);
	}

	/**
	 * Returns the fully qualified name of a transformation matrix.
	 */
	public static String getMatrixName(int id) {
		return MATRIX_CLASS + "." + MATRIX_PREFIX + id;
	}

	/**
	 * Returns the simple name of a transformation matrix.
	 */
	public static String getMatrixNameSimple(int id) {

		return MATRIX_PREFIX + id;

	}

	private String createArrayLiteral(int min, int max, Map<Integer, Integer> valueToState) {
		StringBuilder m = new StringBuilder();
		m.append("{ ");
		for (int tokenValue = min; tokenValue <= max; tokenValue++) {
			Integer state = valueToState.get(tokenValue);
			if (state == null) {
				m.append("-1");
			} else {
				m.append(state);
			}
			if (tokenValue < max) {
				m.append(", ");
			}
		}
		m.append("}");
		String matrix = m.toString();
		return matrix;
	}

	/**
	 * Number for if/else statements.
	 */
	public int size() {
		return this.tokenToStateMap.size();
	}

	@Override
	public String toString() {
		String s = bElse ? "else " : "";
		s += "if (";
		s += (tokenVarName != null) ? tokenVarName : "??";
		s += "==..";
		s += (condition != null) ? " " + condition : "";
		s += ") - ";
		if (tokenToStateMap != null) {
			s += tokenToStateMap.size() + " times";
		} else {
			s += " not analyzed yet.";
		}
		return s;
	}

}