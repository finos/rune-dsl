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

import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IdeCrossrefProposalProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.parsing.RosettaNameEscaper;

import jakarta.inject.Inject;

/**
 * Escapes the name a completion inserts. Xtext proposes the plain name of the target, which does
 * not parse when a segment of it is a keyword - accepting a proposal for a type named
 * {@code type} has to insert {@code ^type}.
 */
public class RosettaCrossrefProposalProvider extends IdeCrossrefProposalProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(RosettaCrossrefProposalProvider.class);

	@Inject
	private RosettaNameEscaper nameEscaper;

	@Override
	protected ContentAssistEntry createProposal(IEObjectDescription candidate, CrossReference crossRef,
			ContentAssistContext context) {
		// Same as `super`, except that the name is escaped before the proposal is created rather
		// than after, so that it is the escaped name that is matched against what the user typed.
		String name = escape(getQualifiedNameConverter().toString(candidate.getName()));
		return getProposalCreator().createProposal(name, context, entry -> {
			entry.setSource(candidate);
			entry.setDescription(candidate.getEClass() != null ? candidate.getEClass().getName() : null);
			entry.setKind(ContentAssistEntry.KIND_REFERENCE);
		});
	}

	private String escape(String name) {
		try {
			return nameEscaper.escapeQualifiedName(name);
		} catch (ValueConverterException e) {
			LOGGER.warn("Cannot write '" + name + "' as a Rune name; proposing it unchanged.", e);
			return name;
		}
	}
}
