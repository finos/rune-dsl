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

package com.regnosys.rosetta.ide.contentassist;

import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.IPrefixMatcher;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator;

import com.regnosys.rosetta.parsing.RosettaNameEscaper;

import jakarta.inject.Inject;

/**
 * Matches an escaped name against what the user has typed so far, whether or not they have typed
 * the caret. Xtext compares the two literally, so a proposal of {@code ^type} is offered for
 * {@code ^ty} but not for {@code ty} - without this class, an escaped name can only be completed
 * by typing its caret first.
 */
public class RosettaContentProposalCreator extends IdeContentProposalCreator {
	private static final String ESCAPE = "^";

	@Inject
	private IPrefixMatcher prefixMatcher;
	@Inject
	private RosettaNameEscaper nameEscaper;

	@Override
	public boolean isValidProposal(String proposal, String prefix, ContentAssistContext context) {
		if (super.isValidProposal(proposal, prefix, context)) {
			return true;
		}
		if (proposal == null || !proposal.contains(ESCAPE)) {
			return false;
		}
		// Compare the names the two stand for, and leave the rest of the judgement to `super` by
		// asking it about the proposal alone.
		return prefixMatcher.isCandidateMatchingPrefix(
						nameEscaper.unescapeQualifiedName(proposal), nameEscaper.unescapeQualifiedName(prefix))
				&& super.isValidProposal(proposal, "", context);
	}
}
