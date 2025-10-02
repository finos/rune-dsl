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

package com.regnosys.rosetta.ide.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensDelta;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.formatting2.FormattingOptionsService;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsResolver;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsService;
import com.regnosys.rosetta.ide.overrides.IParentsService;
import com.regnosys.rosetta.ide.overrides.ParentsParams;
import com.regnosys.rosetta.ide.overrides.ParentsResult;
import com.regnosys.rosetta.ide.quickfix.IResolveCodeActionService;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokensService;
import com.regnosys.rosetta.ide.semantictokens.SemanticToken;
import com.regnosys.rosetta.ide.util.CodeActionUtils;

/**
 * TODO: contribute to Xtext.
 *
 */
public class RosettaLanguageServerImpl extends LanguageServerImpl implements RosettaLanguageServer, RosettaTextDocumentService {
	@Inject FormattingOptionsService formattingOptionsService;
	@Inject CodeActionUtils codeActionUtils;
	
	@Override
	public RosettaTextDocumentService getRosettaTextDocumentService() {
		return this;
	}

	/*** INLAY HINTS ***/
	protected List<InlayHint> inlayHint(InlayHintParams params, CancelIndicator cancelIndicator) {
		URI uri = this.getURI(params.getTextDocument());
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			IInlayHintsService service = getService(uri, IInlayHintsService.class);
			List<InlayHint> result = service.computeInlayHint(document, resource, params, cancelIndicator);
			this.installInlayHintURI(result, uri.toString());
			return result;
		});
	}

	@Override
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		return this.getRequestManager().runRead((cancelIndicator) -> this.inlayHint(params, cancelIndicator));
	}

	@Override
	public CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved) {
		URI uri = this.uninstallInlayHintURI(unresolved);
		return uri == null ? CompletableFuture.completedFuture(unresolved) : this.getRequestManager()
			.runRead((cancelIndicator) -> this.resolveInlayHint(uri, unresolved, cancelIndicator));
	}

	protected InlayHint resolveInlayHint(URI uri, InlayHint unresolved, CancelIndicator cancelIndicator) {
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			IInlayHintsResolver resolver = getService(uri, IInlayHintsResolver.class);
			return resolver.resolveInlayHint(document, resource, unresolved, cancelIndicator);
		});
	}

	protected void installInlayHintURI(List<? extends InlayHint> inlayHints, String uri) {
		for (InlayHint inlayHint : inlayHints) {
			Object data = inlayHint.getData();
			if (data != null) {
				inlayHint.setData(Arrays.asList(uri, inlayHint.getData()));
			} else {
				inlayHint.setData(uri);
			}
		}
	}

	protected URI uninstallInlayHintURI(InlayHint inlayHint) {
		URI result = null;
		Object data = inlayHint.getData();
		if (data instanceof String) {
			result = URI.createURI(data.toString());
			inlayHint.setData(null);
		} else if (data instanceof List) {
			List<?> l = (List<?>) data;
			result = URI.createURI(l.get(0).toString());
			inlayHint.setData(l.get(1));
		}

		return result;
	}
	
	/*** SEMANTIC TOKENS ***/
	public List<SemanticToken> semanticTokens(SemanticTokensParams params, CancelIndicator cancelIndicator) {
		URI uri = getURI(params.getTextDocument());
		return getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			return service.computeSemanticTokens(document, resource, params, cancelIndicator);
		});
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/full
	 */
	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		return getRequestManager().runRead((cancelIndicator) -> semanticTokensFull(params, cancelIndicator));
	}
	
	@Override
	protected SemanticTokens semanticTokensFull(SemanticTokensParams params, CancelIndicator cancelIndicator) {
		URI uri = getURI(params.getTextDocument());
		return getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			List<SemanticToken> tokens = service.computeSemanticTokens(document, resource, params, cancelIndicator);
			SemanticTokens result = service.toSemanticTokensResponse(tokens);
			return result;
		});
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/full/delta
	 */
	@Override
	public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(SemanticTokensDeltaParams params) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/range
	 */
	@Override
	public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
		return this.getRequestManager().runRead((cancelIndicator) -> this.semanticTokensRange(params, cancelIndicator));
	}
	
	protected SemanticTokens semanticTokensRange(SemanticTokensRangeParams params, CancelIndicator cancelIndicator) {
		URI uri = getURI(params.getTextDocument());
		return getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			List<SemanticToken> tokens = service.computeSemanticTokensInRange(document, resource, params, cancelIndicator);
			SemanticTokens result = service.toSemanticTokensResponse(tokens);
			return result;
		});
	}

	/*** FORMATTING ***/
	@Override
	public CompletableFuture<FormattingOptions> getDefaultFormattingOptions() {
		return CompletableFuture.completedFuture(formattingOptionsService.getDefaultOptions());
	}
	
	/*** CODE ACTIONS ***/
	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
		return getRequestManager().runRead((cancelIndicator) -> this.resolveCodeAction(unresolved, cancelIndicator));
	}
	
	protected CodeAction resolveCodeAction(CodeAction codeAction, CancelIndicator cancelIndicator) {
		CodeActionParams codeActionParams = codeActionUtils.getCodeActionParams(codeAction);

		if (codeActionParams.getTextDocument() == null) {
			return null;
		}

		URI uri = getURI(codeActionParams.getTextDocument());
		
		return getWorkspaceManager().doRead(uri, (doc, resource) -> {
			ICodeActionService2.Options baseOptions = createCodeActionBaseOptions(doc,
					resource, getLanguageServerAccess(), codeActionParams, cancelIndicator);
			
			IResolveCodeActionService resolveCodeActionService = getService(uri, IResolveCodeActionService.class);
			return resolveCodeActionService.getCodeActionResolution(codeAction, baseOptions);
		});
	}

	private Options createCodeActionBaseOptions(Document doc, XtextResource resource,
			ILanguageServerAccess languageServerAcces, CodeActionParams codeActionParams,
			CancelIndicator cancelIndicator) {
		Options baseOptions = new ICodeActionService2.Options();
		baseOptions.setDocument(doc);
		baseOptions.setResource(resource);
		baseOptions.setLanguageServerAccess(languageServerAcces);
		baseOptions.setCodeActionParams(codeActionParams);
		baseOptions.setCancelIndicator(cancelIndicator);

		return baseOptions;
	}

	/*** PARENTS ***/
	@Override
	public CompletableFuture<List<? extends ParentsResult>> parents(ParentsParams params) {
		return getRequestManager().runRead((cancelIndicator) -> this.parents(params, cancelIndicator));
	}
	
	protected List<? extends ParentsResult> parents(ParentsParams params, CancelIndicator cancelIndicator) {
		URI uri = this.getURI(params.getTextDocument());
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			IParentsService service = getService(uri, IParentsService.class);
			return service.computeParents(document, resource, params, cancelIndicator);
		});
	}
}