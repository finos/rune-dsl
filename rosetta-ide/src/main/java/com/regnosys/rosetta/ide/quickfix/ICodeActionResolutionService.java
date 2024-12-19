package com.regnosys.rosetta.ide.quickfix;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;

public interface ICodeActionResolutionService {
	CompletableFuture<CodeAction> getCodeActionResolution(CodeAction unresolved);
}
