/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.parsing;

import java.util.List;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.IDValueConverter;

import com.regnosys.rosetta.services.RosettaGrammarAccess;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * The single place that knows how a name is written in a Rune model: names that collide with a
 * keyword are escaped with a caret, e.g. {@code ^type}. Every part of that - the escape
 * character, what a name may contain, what separates the segments of a qualified one, and the
 * wildcard an import ends in - is read off the grammar rather than restated here.
 *
 * <p>The parser and the serializer reach this through the value converters registered in
 * {@link RosettaValueConverterService}. Code that writes Rune source by hand - organising
 * imports, proposing a completion - should call one of the escape methods rather than writing a
 * name straight into the document.
 */
@Singleton
public class RosettaNameEscaper {
	private final EscapableIDValueConverter validIDConverter;
	private final EscapableIDValueConverter typeParameterValidIDConverter;
	private final EscapableQualifiedNameValueConverter qualifiedNameConverter;
	private final EscapableQualifiedNameValueConverter importedNamespaceConverter;

	@Inject
	public RosettaNameEscaper(RosettaGrammarAccess grammarAccess, IDValueConverter idConverter) {
		// Binding the converter to the `ID` terminal rule is what makes it escape and validate
		// against the grammar: it escapes with the terminal's own escape character, and checks
		// what it produces by lexing it back.
		idConverter.setRule(grammarAccess.getIDRule());

		String separator = onlyKeywordOf(grammarAccess.getQualifiedNameRule());
		String wildcard = keywordsOf(grammarAccess.getQualifiedNameWithWildcardRule()).stream()
				.filter(keyword -> !keyword.equals(separator))
				.reduce((first, second) -> {
					throw new IllegalStateException("Expected `"
							+ grammarAccess.getQualifiedNameWithWildcardRule().getName()
							+ "` to add a single keyword to `" + grammarAccess.getQualifiedNameRule().getName()
							+ "`, but it adds " + first + " and " + second + ".");
				})
				.orElseThrow(() -> new IllegalStateException("Expected `"
						+ grammarAccess.getQualifiedNameWithWildcardRule().getName()
						+ "` to add a wildcard keyword to `" + grammarAccess.getQualifiedNameRule().getName()
						+ "`, but it adds none."));

		this.validIDConverter = new EscapableIDValueConverter(grammarAccess.getValidIDRule(), idConverter);
		this.typeParameterValidIDConverter =
				new EscapableIDValueConverter(grammarAccess.getTypeParameterValidIDRule(), idConverter);
		this.qualifiedNameConverter =
				new EscapableQualifiedNameValueConverter(validIDConverter, separator, null);
		this.importedNamespaceConverter =
				new EscapableQualifiedNameValueConverter(validIDConverter, separator, wildcard);
	}

	/**
	 * Writes a single name as Rune source, e.g. {@code type} becomes {@code ^type}.
	 *
	 * @throws ValueConverterException if the name cannot be written as a Rune identifier.
	 */
	public String escapeName(String name) {
		return validIDConverter.toString(name);
	}

	/**
	 * Writes a dotted name as Rune source, e.g. {@code namespace.foo} becomes
	 * {@code ^namespace.foo}. Each segment is escaped on its own.
	 *
	 * @throws ValueConverterException if a segment cannot be written as a Rune identifier.
	 */
	public String escapeQualifiedName(String qualifiedName) {
		return qualifiedNameConverter.toString(qualifiedName);
	}

	/**
	 * Writes the namespace of an import as Rune source, e.g. {@code namespace.foo.*} becomes
	 * {@code ^namespace.foo.*}. As {@link #escapeQualifiedName(String)}, except that the name may
	 * end in a wildcard.
	 *
	 * @throws ValueConverterException if a segment cannot be written as a Rune identifier.
	 */
	public String escapeImportedNamespace(String importedNamespace) {
		return importedNamespaceConverter.toString(importedNamespace);
	}

	/**
	 * The name a dotted name written as Rune source stands for, e.g. {@code ^namespace.foo}
	 * becomes {@code namespace.foo}.
	 */
	public String unescapeQualifiedName(String qualifiedName) {
		return qualifiedNameConverter.toValue(qualifiedName, null);
	}

	/** Whether a name written as Rune source carries an escape on any of its segments. */
	public boolean isEscaped(String qualifiedName) {
		return !qualifiedName.equals(unescapeQualifiedName(qualifiedName));
	}

	EscapableIDValueConverter getValidIDConverter() {
		return validIDConverter;
	}

	EscapableIDValueConverter getTypeParameterValidIDConverter() {
		return typeParameterValidIDConverter;
	}

	EscapableQualifiedNameValueConverter getQualifiedNameConverter() {
		return qualifiedNameConverter;
	}

	EscapableQualifiedNameValueConverter getImportedNamespaceConverter() {
		return importedNamespaceConverter;
	}

	private static String onlyKeywordOf(AbstractRule rule) {
		List<String> keywords = keywordsOf(rule);
		if (keywords.size() != 1) {
			throw new IllegalStateException("Expected `" + rule.getName()
					+ "` to contain a single keyword, but it contains " + keywords + ".");
		}
		return keywords.get(0);
	}

	/** The keywords the rule itself spells out, not those of the rules it calls. */
	private static List<String> keywordsOf(AbstractRule rule) {
		return EcoreUtil2.getAllContentsOfType(rule, Keyword.class).stream()
				.map(Keyword::getValue)
				.distinct()
				.toList();
	}
}
