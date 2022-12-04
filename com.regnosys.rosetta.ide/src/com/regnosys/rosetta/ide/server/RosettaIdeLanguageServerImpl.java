package com.regnosys.rosetta.ide.server;

import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.*;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.ide.inlayhints.IInlayHintsResolver;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RosettaIdeLanguageServerImpl extends LanguageServerImpl  {

	@Override
	protected ServerCapabilities createServerCapabilities(InitializeParams params) {
		ServerCapabilities serverCapabilities = super.createServerCapabilities(params);
		IResourceServiceProvider resourceServiceProvider = getResourceServiceProvider(URI.createURI("synth:///file.rosetta"));

		if (resourceServiceProvider.get(IInlayHintsService.class) != null) {
			InlayHintRegistrationOptions inlayHintRegistrationOptions = new InlayHintRegistrationOptions();
			inlayHintRegistrationOptions.setResolveProvider(resourceServiceProvider.get(IInlayHintsResolver.class) != null);
			serverCapabilities.setInlayHintProvider(inlayHintRegistrationOptions);
		}
		return serverCapabilities;
	}

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

	@SuppressWarnings("rawtypes")
	protected URI uninstallInlayHintURI(InlayHint inlayHint) {
		URI result = null;
		Object data = inlayHint.getData();
		if (data instanceof String) {
			result = URI.createURI(data.toString());
			inlayHint.setData(null);
		} else if (data instanceof List) {
			List<?> l = (List) data;
			result = URI.createURI(l.get(0).toString());
			inlayHint.setData(l.get(1));
		}

		return result;
	}
}