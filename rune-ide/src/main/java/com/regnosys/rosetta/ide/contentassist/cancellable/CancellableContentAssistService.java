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

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.contentassist.ContentAssistService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.TextRegion;
import org.eclipse.xtext.xbase.lib.Exceptions;

import com.google.common.base.Strings;

/**
 * A patch of Xtext's `CancellableContentAssistService` which makes a completion request cancellable.
 * TODO: contribute to Xtext.
 */
public class CancellableContentAssistService extends ContentAssistService {
	@Inject
	private Provider<CancellableContentAssistContextFactory> contextFactoryProvider;

	@Inject
	private ExecutorService executorService;
	
	@Inject
	private Provider<IdeContentProposalAcceptor> proposalAcceptorProvider;
	
	@Inject
	private IdeContentProposalProvider proposalProvider;

	@Inject
	private OperationCanceledManager operationCanceledManager;
	
	// Patch of super.createCompletionList that passes the `cancelIndicator` on
	// to the `createProposals` method.
	@Override
	public CompletionList createCompletionList(Document document, XtextResource resource, CompletionParams params,
			CancelIndicator cancelIndicator) {
		try {
			CompletionList result = new CompletionList();
			result.setIsIncomplete(true);
			IdeContentProposalAcceptor acceptor = proposalAcceptorProvider.get();
			int caretOffset = document.getOffSet(params.getPosition());
			Position caretPosition = params.getPosition();
			TextRegion position = new TextRegion(caretOffset, 0);
			try {
				createProposals(document.getContents(), position, caretOffset, resource, acceptor, cancelIndicator);
			} catch (Throwable t) {
				if (!operationCanceledManager.isOperationCanceledException(t)) {
					throw t;
				}
			}
			int idx = 0;
			for (ContentAssistEntry it : acceptor.getEntries()) {
				CompletionItem item = toCompletionItem(it, caretOffset, caretPosition, document);
				item.setSortText(Strings.padStart(Integer.toString(idx), 5, '0'));
				result.getItems().add(item);
				idx++;
			}
			return result;
		} catch (Throwable e) {
			throw Exceptions.sneakyThrow(e);
		}
	}

	// Patch of super.createProposals that accepts a `cancelIndicator`.
	protected void createProposals(String document, TextRegion selection, int caretOffset, XtextResource resource,
			IIdeContentProposalAcceptor acceptor, CancelIndicator cancelIndicator) {
		if (caretOffset > document.length()) {
			return;
		}
		CancellableContentAssistContextFactory contextFactory = contextFactoryProvider.get();
		contextFactory.setPool(executorService);
		contextFactory.setCancelIndicator(cancelIndicator);
		ContentAssistContext[] contexts = contextFactory.create(document, selection, caretOffset, resource);
		proposalProvider.createProposals(Arrays.asList(contexts), acceptor);
	}
}
