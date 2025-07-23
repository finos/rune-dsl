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

package com.regnosys.rosetta.ide.contentassist.cancellable;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.inject.Inject;

import org.antlr.runtime.TokenSource;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.UnorderedGroup;
import org.eclipse.xtext.ide.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ObservableXtextTokenStream;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.InfiniteRecursion;
import org.eclipse.xtext.parser.antlr.IUnorderedGroupHelper;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.ide.contentassist.antlr.RosettaParser;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

/**
 * A patch of the generated `RosettaParser` which makes a completion request cancellable.
 * TODO: contribute to Xtext.
 */
public class CancellableRosettaParser extends RosettaParser implements ICancellableContentAssistParser {

	@Inject
	private RosettaGrammarAccess grammarAccess;
	
	@Inject
	private OperationCanceledManager operationCanceledManager;

	protected CancellableInternalRosettaParser createParser(CancelIndicator cancelIndicator) {
		CancellableInternalRosettaParser result = new CancellableInternalRosettaParser(null, operationCanceledManager, cancelIndicator);
		result.setGrammarAccess(grammarAccess);
		return result;
	}

	// A patch of super.getFollowElements which uses an internal parser that supports
	// cancellation.
	@Override
	public Collection<FollowElement> getFollowElements(String input, boolean strict, CancelIndicator cancelIndicator) {
		TokenSource tokenSource = createTokenSource(input);
		CancellableInternalRosettaParser parser = createParser(cancelIndicator);
		parser.setStrict(strict);
		ObservableXtextTokenStream tokens = new ObservableXtextTokenStream(tokenSource, parser);
		tokens.setInitialHiddenTokens(getInitialHiddenTokens());
		parser.setTokenStream(tokens);
		IUnorderedGroupHelper helper = createUnorderedGroupHelper();
		parser.setUnorderedGroupHelper(helper);
		helper.initializeWith(parser);
		tokens.setListener(parser);
		try {
			return Lists.newArrayList(getFollowElements(parser));
		} catch (InfiniteRecursion infinite) {
			return Lists.newArrayList(parser.getFollowElements());
		}
	}

	// A patch of super.getFollowElements which uses an internal parser that supports
	// cancellation.
	@Override
	public Collection<FollowElement> getFollowElements(FollowElement element, CancelIndicator cancelIndicator) {
		if (element.getLookAhead() <= 1)
			throw new IllegalArgumentException("lookahead may not be less than or equal to 1");
		Collection<FollowElement> result = new ArrayList<>();
		for (AbstractElement elementToParse : getElementsToParse(element)) {
			elementToParse = unwrapSingleElementGroups(elementToParse);
			String ruleName = getRuleName(elementToParse);
			String[][] allRuleNames = getRequiredRuleNames(ruleName, element.getParamStack(), elementToParse);
			for (String[] ruleNames : allRuleNames) {
				for (int i = 0; i < ruleNames.length; i++) {
					CancellableInternalRosettaParser parser = createParser(cancelIndicator);
					parser.setUnorderedGroupHelper(createUnorderedGroupHelper());
					parser.getUnorderedGroupHelper().initializeWith(parser);
					ObservableXtextTokenStream tokens = setTokensFromFollowElement(parser, element);
					tokens.setListener(parser);
					parser.getGrammarElements().addAll(element.getTrace());
					parser.getGrammarElements().add(elementToParse);
					parser.getLocalTrace().addAll(element.getLocalTrace());
					parser.getLocalTrace().add(elementToParse);
					parser.getParamStack().addAll(element.getParamStack());
					if (elementToParse instanceof UnorderedGroup && element.getGrammarElement() == elementToParse) {
						UnorderedGroup group = (UnorderedGroup) elementToParse;
						IUnorderedGroupHelper helper = getInitializedUnorderedGroupHelper(element, parser, group);
						parser.setUnorderedGroupHelper(ignoreFirstEntrance(helper));
					}
					Collection<FollowElement> elements = getFollowElements(parser, elementToParse, ruleNames, i);
					result.addAll(elements);
				}
			}
		}
		return result;
	}
}
