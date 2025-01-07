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

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.formatting2.FormattingOptionsAdaptor;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsResolver;
import com.regnosys.rosetta.ide.inlayhints.IInlayHintsService;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokensService;
import com.regnosys.rosetta.ide.semantictokens.SemanticToken;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * TODO: contribute to Xtext.
 */
public class RosettaLanguageServerImpl extends LanguageServerImpl implements RosettaLanguageServer {
    @Inject
    FormattingOptionsAdaptor formattingOptionsAdapter;

    @Override
    protected ServerCapabilities createServerCapabilities(InitializeParams params) {
        ServerCapabilities serverCapabilities = super.createServerCapabilities(params);
        IResourceServiceProvider resourceServiceProvider = getResourceServiceProvider(URI.createURI("synth:///file.rosetta"));

        if (resourceServiceProvider.get(IInlayHintsService.class) != null) {
            InlayHintRegistrationOptions inlayHintRegistrationOptions = new InlayHintRegistrationOptions();
            inlayHintRegistrationOptions.setResolveProvider(resourceServiceProvider.get(IInlayHintsResolver.class) != null);
            serverCapabilities.setInlayHintProvider(inlayHintRegistrationOptions);
        }

        if (resourceServiceProvider.get(ICodeActionService2.class) != null) {
            CodeActionOptions codeActionProvider = new CodeActionOptions();
            codeActionProvider.setResolveProvider(true);
            codeActionProvider.setCodeActionKinds(List.of(CodeActionKind.QuickFix));
            codeActionProvider.setWorkDoneProgress(true);
            serverCapabilities.setCodeActionProvider(codeActionProvider);
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

    @Override
    public CompletableFuture<FormattingOptions> getDefaultFormattingOptions() {
        try {
            return CompletableFuture.completedFuture(formattingOptionsAdapter.readFormattingOptions(null));
        } catch (IOException e) {
            // should never happen, since null path always leads to default options being returned
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        return getRequestManager().runRead((cancelIndicator) -> resolveCodeAction(unresolved, cancelIndicator));
    }

    protected CodeAction resolveCodeAction(CodeAction codeAction, CancelIndicator cancelIndicator) {
        CodeActionParams codeActionParams = getCodeActionParams(codeAction);

        if (codeActionParams.getTextDocument() == null) {
            return null;
        }

        URI uri = URI.createURI(codeActionParams.getTextDocument().getUri());

        IQuickFixProvider quickfixes = getService(uri, IQuickFixProvider.class);

        return getWorkspaceManager().doRead(uri, (doc, resource) -> {
            ICodeActionService2.Options options = new ICodeActionService2.Options();
            options.setDocument(doc);
            options.setResource(resource);
            options.setLanguageServerAccess(getLanguageServerAccess());
            options.setCodeActionParams(codeActionParams);
            options.setCancelIndicator(cancelIndicator);

            Diagnostic diagnostic = Iterables.getOnlyElement(codeAction.getDiagnostics());
            List<DiagnosticResolution> resolutions = quickfixes.getResolutions(options, diagnostic).stream()
                    .sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
                    .collect(Collectors.toList());
            // wrong.
            resolutions.stream()
                    .filter(r -> r.getLabel().equals(codeAction.getTitle()))
                    .findFirst()
                    .ifPresent(r -> codeAction.setEdit(r.apply()));

            return codeAction;
        });
    }

    protected CodeActionParams getCodeActionParams(CodeAction codeAction) {
        Object data = codeAction.getData();
        CodeActionParams codeActionParams = null;
        if (data instanceof CodeActionParams) {
            codeActionParams = (CodeActionParams) data;
        }
        if (data instanceof JsonObject) {
            Gson gson = new MessageJsonHandler(Map.of()).getGson();
            codeActionParams = gson.fromJson(((JsonObject) data), CodeActionParams.class);
        }
        return codeActionParams;
    }
}