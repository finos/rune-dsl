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
import java.util.stream.Collectors;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.conversion.impl.IDValueConverter;
import org.eclipse.xtext.nodemodel.INode;

/**
 * Converts between an identifier as it is written in a Rune model - possibly escaped with a
 * caret, e.g. {@code ^type} - and the name it stands for, e.g. {@code type}.
 *
 * <p>Which keywords this rule accepts as a name is read off the rule itself, following its calls
 * to other parser rules: {@code condition} in {@code ValidID}, and {@code min} and {@code max} on
 * top of those in {@code TypeParameterValidID}. Adding a keyword to such a rule therefore needs
 * no change here. Everything else about how an identifier is written - the escape character, the
 * characters it may contain, and which keywords have to be escaped at all - comes from the
 * {@code ID} terminal rule by way of {@code idConverter}.
 */
public class EscapableIDValueConverter extends AbstractValueConverter<String> {
	private final Set<String> keywordsUsableAsIdentifier;
	private final IDValueConverter idConverter;

	/**
	 * @param rule the rule this converter belongs to, which decides the keywords it may leave
	 *            unescaped.
	 * @param idConverter a converter bound to the {@code ID} terminal rule, so that it escapes and
	 *            validates against the grammar rather than against a restatement of it.
	 */
	public EscapableIDValueConverter(AbstractRule rule, IDValueConverter idConverter) {
		this.keywordsUsableAsIdentifier = collectKeywords(rule, new HashSet<>());
		this.idConverter = idConverter;
	}

	@Override
	public String toValue(String string, INode node) {
		return idConverter.toValue(string, node);
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		if (keywordsUsableAsIdentifier.contains(value)) {
			// This rule accepts the keyword as a name, so an escape would only add noise.
			return value;
		}
		return idConverter.toString(value);
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
