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

package com.regnosys.rosetta.generator.java.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import java.util.stream.Collectors;

public class RosettaGrammarUtil {

	/**
	 * Quotes the given text as a Java string literal, continuing the literal on a
	 * new line, indented with a tab, after each line break. For Xtend templates,
	 * such as the C# data rule generator in rosetta-code-generators; use
	 * {@link #quoteForCodeWriter(String)} with a {@code CodeWriter}.
	 */
	public static String quote(String text) {
		return "\"" + escapeLines(text).replace("\n", "\\n\" + \n\t\"") + "\"";
	}

	/**
	 * Quotes the given text as a Java string literal, continuing the literal on a
	 * new line after each line break. The continuation lines carry no indentation
	 * of their own: a {@code CodeWriter} indents every line it writes.
	 */
	public static String quoteForCodeWriter(String text) {
		return "\"" + escapeLines(text).replace("\n", "\\n\" +\n\"") + "\"";
	}

	/**
	 * Escapes model text for a Java string literal and normalises its line breaks to single
	 * {@code \n}s. Backslashes are escaped before quotes, so the backslash added in front of a
	 * quote is not doubled again. Doubling every backslash also stops the Java compiler from
	 * reading a backslash followed by {@code u} as a unicode escape.
	 */
	private static String escapeLines(String text) {
		return text.trim()
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n\n", "\n");
	}

	public static String extractNodeText(EObject rosettaFeature, EStructuralFeature feature) {
		return NodeModelUtils.findNodesForFeature(rosettaFeature, feature).stream()
				.map(NodeModelUtils::getTokenText)
				.collect(Collectors.joining());
	}
}
