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

import java.util.Collection;
import java.util.Collections;

import jakarta.inject.Inject;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.ide.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Strings;

/**
 * A patch of Xtext's `ContentAssistContextFactory` which makes a completion request cancellable.
 * TODO: contribute to Xtext.
 */
public class CancellableContentAssistContextFactory extends ContentAssistContextFactory {
	private CancelIndicator cancelIndicator;
	private String document;
	
	@Inject
	protected ICancellableContentAssistParser cancellableParser;
	
	@Inject
	private OperationCanceledManager operationCanceledManager;
	
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;
	}
	
	@Override
	public ContentAssistContext[] create(String document, ITextRegion selection, int offset, XtextResource resource) {
		this.document = document;
		return super.create(document, selection, offset, resource);
	}
	
	@Override
	protected ContentAssistContext[] doCreateContexts(int offset) {
		ContentAssistContext[] result = super.doCreateContexts(offset);
		operationCanceledManager.checkCanceled(cancelIndicator);
		return result;
	}
	
	// Patch of super.handleLastCompleteNodeIsAtEndOfDatatypeNode
	// that uses the custom `getFollowElements` method.
	@Override
	protected void handleLastCompleteNodeIsAtEndOfDatatypeNode() {
		String prefix = getPrefix(lastCompleteNode);
		String completeInput = getInputToParse(lastCompleteNode);
		INode previousNode = getLastCompleteNodeByOffset(rootNode, lastCompleteNode.getOffset());
		EObject previousModel = previousNode.getSemanticElement();
		INode currentDatatypeNode = getContainingDatatypeRuleNode(currentNode);
		Collection<FollowElement> followElements = getFollowElements(completeInput, false);
		int prevSize = contextBuilders.size();
		doCreateContexts(previousNode, currentDatatypeNode, prefix, previousModel, followElements);
		
		if (lastCompleteNode instanceof ILeafNode && lastCompleteNode.getGrammarElement() == null && contextBuilders.size() != prevSize) {
			handleLastCompleteNodeHasNoGrammarElement(contextBuilders.subList(prevSize, contextBuilders.size()), previousModel);
		}
	}
	
	// Patch of super.handleLastCompleteNodeAsPartOfDatatypeNode
	// that uses the custom `getFollowElements` method.
	@Override
	protected void handleLastCompleteNodeAsPartOfDatatypeNode() {
		String prefix = getPrefix(datatypeNode);
		String completeInput = getInputToParse(datatypeNode);
		Collection<FollowElement> followElements = getFollowElements(completeInput, false);
		INode lastCompleteNodeBeforeDatatype = getLastCompleteNodeByOffset(rootNode, datatypeNode.getTotalOffset());
		doCreateContexts(lastCompleteNodeBeforeDatatype, datatypeNode, prefix, currentModel, followElements);
	}
	
	// Patch of super.createContextsForLastCompleteNode
	// that uses the custom `getFollowElements` method.
	@Override
	protected void createContextsForLastCompleteNode(EObject previousModel, boolean strict) {
		String currentNodePrefix = getPrefix(currentNode);
		if (!Strings.isEmpty(currentNodePrefix) && !currentNode.getText().equals(currentNodePrefix)) {
			lexer.setCharStream(new ANTLRStringStream(currentNodePrefix));
			Token token = lexer.nextToken();
			if (token == Token.EOF_TOKEN) { // error case - nothing could be parsed
				return;
			}
			while(token != Token.EOF_TOKEN) {
				if (isErrorToken(token))
					return;
				token = lexer.nextToken();
			}
		}
		String prefix = "";
		String completeInput = getInputToParse(document, completionOffset);
		Collection<FollowElement> followElements = getFollowElements(completeInput, strict);
		doCreateContexts(lastCompleteNode, currentNode, prefix, previousModel, followElements);
	}
	
	private Collection<FollowElement> getFollowElements(String completeInput, boolean strict) {
		try {
			return cancellableParser.getFollowElements(completeInput, strict, cancelIndicator);
		} catch(Exception ex) {
			if (operationCanceledManager.isOperationCanceledException(ex)) {
				return Collections.emptyList();
			}
			throw ex;
		}
	}
}
