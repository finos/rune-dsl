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

package com.regnosys.rosetta.parsing;

import java.util.Set;

import jakarta.inject.Inject;

import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.conversion.impl.IDValueConverter;
import org.eclipse.xtext.nodemodel.INode;

import com.google.common.collect.ImmutableSet;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

/**
 * Allows using a caret `^` to escape identifiers based on the `ValidID` rule. 
 * 
 * Similar implementation to {@link org.eclipse.xtext.conversion.impl.IDValueConverter}.
 */
public class ValidIDConverter extends AbstractValueConverter<String> {
	private final ParserRule validIDRule;
	private final Set<String> validKeywordIDs;
	private final IDValueConverter delegate;
	@Inject
	public ValidIDConverter(RosettaGrammarAccess grammarAccess, IDValueConverter delegate) {
		this.validIDRule = grammarAccess.getValidIDRule();
		this.delegate = delegate;
		
		// We expect the ValidID rule to be of the form `ID | <list of keywords>`
		Alternatives alternatives = (Alternatives) validIDRule.getAlternatives();
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		alternatives.getElements().stream().skip(1).forEach(element -> builder.add(((Keyword)element).getValue()));
		validKeywordIDs = builder.build();
	}

	@Override
	public String toValue(String string, INode node) {
		return delegate.toValue(string, node);
	}
	
	@Override
	public String toString(String value) throws ValueConverterException {
		if (validKeywordIDs.contains(value)) {
			return value;
		}
		return delegate.toString(value);
	}
}
