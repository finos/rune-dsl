package com.regnosys.rosetta.ide.server;

import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.ide.inlayhints.IInlayHintsResolver;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsService;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokensService;
import com.regnosys.rosetta.ide.semantictokens.SemanticToken;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RosettaLanguageServerImpl extends LanguageServerImpl  {

	@Override
	protected ServerCapabilities createServerCapabilities(InitializeParams params) {
		ServerCapabilities serverCapabilities = super.createServerCapabilities(params);
		IResourceServiceProvider resourceServiceProvider = getResourceServiceProvider(URI.createURI("synth:///file.rosetta"));

		if (resourceServiceProvider.get(IInlayHintsService.class) != null) {
			InlayHintRegistrationOptions inlayHintRegistrationOptions = new InlayHintRegistrationOptions();
			inlayHintRegistrationOptions.setResolveProvider(resourceServiceProvider.get(IInlayHintsResolver.class) != null);
			serverCapabilities.setInlayHintProvider(inlayHintRegistrationOptions);
		}
		
		ISemanticTokensService semanticTokensService = resourceServiceProvider.get(ISemanticTokensService.class);
		if (semanticTokensService != null) {
			SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
			semanticTokensOptions.setLegend(semanticTokensService.getLegend());
			semanticTokensOptions.setFull(true);
			semanticTokensOptions.setRange(true);
			serverCapabilities.setSemanticTokensProvider(semanticTokensOptions);
		}
		
		return serverCapabilities;
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
		URI uri = this.getURI(params.getTextDocument());
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			return service.computeSemanticTokens(document, resource, params, cancelIndicator);
		});
	}
	
	protected SemanticTokens semanticTokensFull(SemanticTokensParams params, CancelIndicator cancelIndicator) {
		URI uri = this.getURI(params.getTextDocument());
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			List<SemanticToken> tokens = service.computeSemanticTokens(document, resource, params, cancelIndicator);
			SemanticTokens result = service.toSemanticTokensResponse(tokens);
			return result;
		});
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/full
	 */
	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		return this.getRequestManager().runRead((cancelIndicator) -> this.semanticTokensFull(params, cancelIndicator));
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/full/delta
	 */
	@Override
	public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(SemanticTokensDeltaParams params) {
		throw new UnsupportedOperationException();
	}
	
	protected SemanticTokens semanticTokensRange(SemanticTokensRangeParams params, CancelIndicator cancelIndicator) {
		URI uri = this.getURI(params.getTextDocument());
		return this.getWorkspaceManager().doRead(uri, (document, resource) -> {
			ISemanticTokensService service = getService(uri, ISemanticTokensService.class);
			List<SemanticToken> tokens = service.computeSemanticTokensInRange(document, resource, params, cancelIndicator);
			SemanticTokens result = service.toSemanticTokensResponse(tokens);
			return result;
		});
	}
	
	/**
	 * LSP method: textDocument/semanticTokens/range
	 */
	@Override
	public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
		return this.getRequestManager().runRead((cancelIndicator) -> this.semanticTokensRange(params, cancelIndicator));
	}
}