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

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.conversion.ValueConverterException;

import com.regnosys.rosetta.services.RosettaGrammarAccess;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * The single place that knows how a name is written in a Rune model: names that collide with a
 * keyword are escaped with a caret, e.g. {@code ^type}.
 *
 * <p>The parser and the serializer reach this through the value converters registered in
 * {@link RosettaValueConverterService}. Code that writes Rune source by hand - organising
 * imports, proposing a completion - should call {@link #escapeName(String)} or
 * {@link #escapeQualifiedName(String)} rather than writing a name straight into the document.
 */
@Singleton
public class RosettaNameEscaper {
	private final EscapableIDValueConverter validIDConverter;
	private final EscapableIDValueConverter typeParameterValidIDConverter;
	private final EscapableQualifiedNameValueConverter qualifiedNameConverter;
	private final EscapableQualifiedNameValueConverter importedNamespaceConverter;

	@Inject
	public RosettaNameEscaper(RosettaGrammarAccess grammarAccess) {
		Grammar grammar = grammarAccess.getGrammar();
		this.validIDConverter = new EscapableIDValueConverter(grammar, grammarAccess.getValidIDRule());
		this.typeParameterValidIDConverter =
				new EscapableIDValueConverter(grammar, grammarAccess.getTypeParameterValidIDRule());
		this.qualifiedNameConverter = new EscapableQualifiedNameValueConverter(validIDConverter, false);
		this.importedNamespaceConverter = new EscapableQualifiedNameValueConverter(validIDConverter, true);
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
}
