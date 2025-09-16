package com.regnosys.rosetta.ide.quickfix;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

public interface ICodeActionProvider {
	List<CodeAction> getCodeActions(Options options);

	CodeAction resolve(CodeAction unresolved, Options options);
}
