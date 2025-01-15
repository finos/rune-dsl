package com.regnosys.rosetta.ide.quickfix;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

public interface IResolveCodeActionService {
	CodeAction getCodeActionResolution(CodeAction unresolved, IQuickFixProvider quickfixes, Options baseOptions);
	
	Options createCodeActionBaseOptions(Document doc, XtextResource resource, ILanguageServerAccess languageServerAcces, CodeActionParams codeActionParams, CancelIndicator cancelIndicator);
}
