package com.regnosys.rosetta.ide.server;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

public interface RosettaLanguageServer extends LanguageServer {

	@JsonRequest
	CompletableFuture<FormattingOptions> getDefaultFormattingOptions();
	
	@JsonDelegate
	RosettaTextDocumentService getRosettaTextDocumentService();
}
