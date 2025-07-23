package com.regnosys.rosetta.ide.quickfix;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

public interface IResolveCodeActionService {
	CodeAction getCodeActionResolution(CodeAction unresolved, Options baseOptions);
}
