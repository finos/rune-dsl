package com.regnosys.rosetta.ide.quickfix;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

public interface ICodeActionProvider {
	List<CodeAction> getCodeActions(Options options);

	List<TextEdit> sortImportsResolution(EObject object);
}
