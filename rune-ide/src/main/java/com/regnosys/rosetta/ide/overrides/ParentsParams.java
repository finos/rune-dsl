package com.regnosys.rosetta.ide.overrides;

import org.eclipse.lsp4j.TextDocumentIdentifier;

public class ParentsParams {
	private TextDocumentIdentifier textDocument;
	
	public ParentsParams() {}
	public ParentsParams(TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}
	
	public TextDocumentIdentifier getTextDocument() {
		return textDocument;
	}
	public void setTextDocument(TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}
}
