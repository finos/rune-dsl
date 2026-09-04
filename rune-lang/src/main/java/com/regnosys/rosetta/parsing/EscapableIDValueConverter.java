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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

/**
 * Converts between an identifier as it is written in a Rune model - possibly escaped with a
 * caret, e.g. {@code ^type} - and the name it stands for, e.g. {@code type}.
 *
 * <p>Which keywords need a caret is derived from the grammar rule this converter belongs to:
 * a keyword that the rule itself accepts as an identifier (e.g. {@code condition} in
 * {@code ValidID}, or {@code min} in {@code TypeParameterValidID}) is written as is, every
 * other keyword of the grammar is escaped. Adding a keyword to such a rule therefore needs no
 * change here.
 */
public class EscapableIDValueConverter extends AbstractValueConverter<String> {
	private static final String ESCAPE = "^";
	private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private final String ruleName;
	private final Set<String> keywordsNeedingEscape;

	public EscapableIDValueConverter(Grammar grammar, AbstractRule rule) {
		this.ruleName = rule.getName();
		Set<String> keywordsUsableAsIdentifier = collectKeywords(rule, new HashSet<>());
		this.keywordsNeedingEscape = GrammarUtil.getAllKeywords(grammar).stream()
				.filter(keyword -> !keywordsUsableAsIdentifier.contains(keyword))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public String toValue(String string, INode node) {
		if (string == null) {
			return null;
		}
		return string.startsWith(ESCAPE) ? string.substring(ESCAPE.length()) : string;
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		if (value == null) {
			throw new ValueConverterException(ruleName + " may not be null.", null, null);
		}
		if (!IDENTIFIER.matcher(value).matches()) {
			throw new ValueConverterException("'" + value + "' is not a valid " + ruleName + ".", null, null);
		}
		return keywordsNeedingEscape.contains(value) ? ESCAPE + value : value;
	}

	/**
	 * All keywords that {@code rule} accepts, following calls to other parser rules. A terminal
	 * rule such as {@code ID} contributes no keywords.
	 */
	private static Set<String> collectKeywords(AbstractRule rule, Set<AbstractRule> visited) {
		if (!visited.add(rule)) {
			return Set.of();
		}
		Set<String> keywords = EcoreUtil2.getAllContentsOfType(rule, Keyword.class).stream()
				.map(Keyword::getValue)
				.collect(Collectors.toCollection(HashSet::new));
		EcoreUtil2.getAllContentsOfType(rule, RuleCall.class).stream()
				.map(RuleCall::getRule)
				.filter(ParserRule.class::isInstance)
				.forEach(called -> keywords.addAll(collectKeywords(called, visited)));
		return keywords;
	}
}
