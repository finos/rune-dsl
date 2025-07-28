package com.regnosys.rosetta.ide.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.regnosys.rosetta.ide.overrides.ParentsParams;
import com.regnosys.rosetta.ide.overrides.ParentsResult;

@JsonSegment("textDocument")
public interface RosettaTextDocumentService extends TextDocumentService {
	@JsonRequest
	default CompletableFuture<List<? extends ParentsResult>> parents(ParentsParams params) {
		throw new UnsupportedOperationException();
	}
}
