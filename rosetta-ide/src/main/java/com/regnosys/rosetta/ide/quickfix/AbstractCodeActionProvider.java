package com.regnosys.rosetta.ide.quickfix;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.TextEditAcceptor;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.regnosys.rosetta.rosetta.RosettaModel;

public abstract class AbstractCodeActionProvider implements ICodeActionProvider {

	private static final Logger LOG = Logger.getLogger(RosettaCodeActionProvider.class);

	@Override
	public CodeAction resolve(CodeAction unresolved, Options options) {
		String title = unresolved.getTitle();
		if (unresolved == null || title == null) {
			return null;
		}

		Method resolutionMethod = findResolutionMethod(getClass(), title);

		if (resolutionMethod != null) {
			try {
				RosettaModel model = (RosettaModel) options.getResource().getContents().get(0);
				List<TextEdit> edits = (List<TextEdit>) resolutionMethod.invoke(this, model);
				
				ILanguageServerAccess languageServerAccess = options.getLanguageServerAccess();
				
				WorkspaceEdit workspaceEdit = new WorkspaceEdit();
				TextEditAcceptor editAcceptor = new TextEditAcceptor(workspaceEdit, languageServerAccess);
				String uri = options.getResource().getURI().toString();
				
				editAcceptor.accept(uri, options.getDocument(), edits);
				unresolved.setEdit(workspaceEdit);
				return unresolved;
			} catch (Exception e) {
				LOG.error("Error resolving code action: " + title, e);
			}
		}
		return unresolved;
	}

	private Method findResolutionMethod(Class<? extends AbstractCodeActionProvider> clazz, String title) {
		return Arrays.stream(clazz.getMethods()).filter(method -> {
			CodeActionResolution annotation = method.getAnnotation(CodeActionResolution.class);
			return annotation != null && annotation.value().equals(title);
		}).findFirst().orElse(null);
	}
}
